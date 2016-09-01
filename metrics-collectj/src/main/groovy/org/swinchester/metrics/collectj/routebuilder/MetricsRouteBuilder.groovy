package org.swinchester.metrics.collectj.routebuilder

import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.apache.camel.builder.RouteBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.swinchester.metrics.collectj.Constants
import org.swinchester.metrics.collectj.http.HawkularRequestProcessor
import org.swinchester.metrics.collectj.http.JolokiaRequestProcessor
import org.swinchester.metrics.collectj.transform.JolokiaAggregatorResponseToHawkularRequest

/**
 * Created by swinchester on 18/08/16.
 */
@Component(value = "metricsRouteBuilder")
class MetricsRouteBuilder extends RouteBuilder {

    @Value('${hawkular.tenant}')
    String hawkularTenant = "openshift"

    @Value('${aggregator.url}')
    String jolokiaAggregatorApiHost

    @Value('${hawkular.url}')
    String hawkularMetricsHost

    @Value('${kube.namespace}')
    String kubeNamespace

    @Value('${kube.label}')
    String kubeLabel;

    @Value('${camel.cron.uri}')
    String camelCronUri

    @Override
    void configure() throws Exception {
        ["brokermemory", "brokerstats", "destinationstats"].each { metricsName ->

            from("direct:" + metricsName).routeId(metricsName)
                    .log("BEGIN collecting stats for " + metricsName)
                    .process(new Processor() {
                        @Override
                        void process(Exchange exchange) throws Exception {
                            String token = "leORr_10ZQNu0O2-SQhMCI347zCArndUR9C-z-iln3A"
                            if (new File('/var/run/secrets/kubernetes.io/serviceaccount/token').exists()){
                                token = new File('/var/run/secrets/kubernetes.io/serviceaccount/token').text
                            }
                            exchange.setProperty("token", token);
                        }
                    }).id(metricsName + "-token-retrieve")
                    .setProperty(Constants.HAWKULAR_METRICS_HOST, constant(hawkularMetricsHost))
                    .setProperty(Constants.HAWKULAR_METRICS_TYPE, constant("gauges"))
                    .setProperty(Constants.HAWKULAR_TENANT, constant(hawkularTenant))
                    .setProperty(Constants.JOLOKIA_API_HOST, constant(jolokiaAggregatorApiHost))
                    .setProperty(Constants.AUTHORIZATION, simple('Bearer ${property.token}'))
                    .setProperty(Constants.KUBE_LABEL, constant(kubeLabel))
                    .setProperty(Constants.KUBE_NAMESPACE, constant(kubeNamespace))
                    .transform().groovy("resource:classpath:groovy/" + metricsName + "/generateJolokiaRequest.groovy").id(metricsName + "-jolokia-generate")
                    .to("log:org.swinchester.jolokia.req?showAll=true&level=debug")
                    .to("direct:callJolokia")
                    .to("log:org.swinchester.jolokia.resp?showAll=true&level=debug")
                    .process(new JolokiaAggregatorResponseToHawkularRequest())
                    .to("log:org.swinchester.hawk.req?showAll=true&level=debug")
                    .to("direct:callHawkular")
                    .to("log:org.swinchester.hawk.resp?showAll=true&level=debug")
                    .log("END collecting stats for " + metricsName);

        }

        from(camelCronUri)
            .multicast()
                .to("direct:destinationstats")
//                .to("direct:brokermemory", "direct:brokerstats", "direct:destinationstats")
            .end()

        from("direct:callJolokia")
            .process(new JolokiaRequestProcessor())

        from("direct:callHawkular")
            .process(new HawkularRequestProcessor())
    }
}
