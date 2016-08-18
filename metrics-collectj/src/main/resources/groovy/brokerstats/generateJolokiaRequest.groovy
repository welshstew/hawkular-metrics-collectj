import groovy.json.JsonOutput
return JsonOutput.toJson( [type:"read",
                          mbean:"org.apache.activemq:type=Broker,brokerName=kube-lookup",
                          attribute: ["CurrentConnectionsCount","TotalConnectionsCount", "TotalConsumerCount", "TotalDequeueCount","TotalMessageCount","TotalProducerCount"]])