import groovy.json.JsonOutput
return JsonOutput.toJson( [type:"read",
                          mbean:"java.lang:type=Memory",
                          attribute: ["HeapMemoryUsage", "NonHeapMemoryUsage"]])
