import groovy.json.JsonOutput

return JsonOutput.toJson( [type:"read",
                          mbean:"org.apache.activemq:type=Broker,brokerName=kube-lookup,destinationType=Queue,destinationName=*",
                          attribute: ["QueueSize","ConsumerCount"]])