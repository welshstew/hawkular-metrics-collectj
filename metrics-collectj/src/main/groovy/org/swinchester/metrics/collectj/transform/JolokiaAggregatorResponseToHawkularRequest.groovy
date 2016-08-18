package org.swinchester.metrics.collectj.transform

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.camel.Exchange
import org.apache.camel.Processor

/**
 * Created by swinchester on 18/08/16.
 */
class JolokiaAggregatorResponseToHawkularRequest implements Processor{
    @Override
    void process(Exchange exchange) throws Exception {
        exchange.in.setBody(generateBody(exchange.in.body))
    }

    def String generateBody(String bodyText) {
        def js = new JsonSlurper().parseText(bodyText)

        def statsList = []

        js.each { listItem ->

            def ts = new Long(listItem.timestamp)
            def podName = listItem.broker
            //get the attributeList (if it is an attribute list) - may just be one attribute
            def attList = []
            def requestAttribute = listItem.request.attribute
            def mbean = listItem.request.mbean
            if (requestAttribute instanceof List) {
                attList = requestAttribute
            } else {
                attList.add(requestAttribute)
            }

            listItem.value.each { val ->
                def valueKeyName = val.key

                switch (valueKeyName) {
                    case ~/^org.*$/:
                        //in this instance the request was a wildcard search, so we need to invent a metric id based on this key
                        //valueKeyName is the ACTUAL mbean
                        val.value.each { it ->
                            statsList.add(constructMetric(valueKeyName, podName, ts, it.key, new Long(it.value)))
                        }
                        break
                    default:
                        //we have the attributeNames here, so construct an id based on the brokerName, mbean, and attributeName
                        if(val.value instanceof Map){
                            val.value.each{key1, val1 ->
                                statsList.add(constructMetric(mbean, podName, ts, valueKeyName + '.' + key1, val1))
                            }
                        }else {
                            statsList.add(constructMetric(mbean, podName, ts, valueKeyName, val.value))
                        }
                        break
                }
            }
        }

        return JsonOutput.toJson(statsList)
    }


    def Map constructMetric(String mbeanId, String podName, Long timestamp, String attributeName, attributeValue){
        String metricId = 'amq.' + generateMetricStartKey(mbeanId, podName) + '.' + attributeName
        return [id: metricId, data: [[timestamp: timestamp * 1000, value: attributeValue]]]
    }

//    Map flatten(Map m, String separator = '.') { m.collectEntries { k, v ->  v instanceof Map ? flatten(v, separator).collectEntries { q, r ->  [(k + separator + q): r] } : [(k):v] } }

    def String generateMetricStartKey(String mbeanId, String podName){

        def valueList = []

        switch (mbeanId) {
            case ~/^org.*$/:
                def tokened1 = mbeanId.tokenize(':')
                def tokened2 = tokened1[1].tokenize(",")
                tokened2.each {
                    def kv = it.tokenize('=')
                    switch(kv[0]){
                        case 'brokerName':
                            def brokerNameValues = kv[1].tokenize("-")
                            valueList.add(brokerNameValues[0] + '-' + brokerNameValues[1] + '-' +brokerNameValues[2])
                            break
                        case 'destinationName':
                            valueList.add(kv[1])
                            break
                    }
                }
                break
            case ~/^java.*$/:
                def podValues = podName.tokenize("-")
                valueList.add(podValues[0] + '-' + podValues[1] + '-' +podValues[2])
                break
            default:
                break
        }
        return valueList.join('.')
    }
}
