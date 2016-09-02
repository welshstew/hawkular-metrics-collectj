package org.swinchester.metrics.collectj.routebuilder

import org.apache.camel.builder.RouteBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Created by swinchester on 2/09/16.
 */
@Component(value = "timerRouteBuilder")
class TimerRouteBuilder extends RouteBuilder{

    @Value('${camel.cron.uri}')
    String camelCronUri

    @Override
    void configure() throws Exception {

        from(camelCronUri)
                .multicast()
                .to("direct:destinationstats")
//                .to("direct:brokermemory", "direct:brokerstats", "direct:destinationstats")
                .end()
    }
}
