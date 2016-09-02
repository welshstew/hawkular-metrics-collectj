package org.swinchester.metrics.collectj.test;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.ModelCamelContext;
import org.apache.commons.io.IOUtils;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;

/**
 * Created by swinchester on 2/09/16.
 */
public class MetricsHelper {

    public static void testMetric(String metricName, ModelCamelContext context, MockEndpoint jolokiaMock, MockEndpoint hawkularMock) throws IOException, InterruptedException {
        String jolokiaRequest = IOUtils.toString(
                MetricsHelper.class.getResourceAsStream("/" + metricName + "/expectedJolokiaRequest-body.json"),
                "UTF-8"
        );

        String hawkularRequest = IOUtils.toString(
                MetricsHelper.class.getResourceAsStream("/" + metricName + "/expectedHawkularRequest-body.json"),
                "UTF-8"
        );

        jolokiaMock.whenAnyExchangeReceived(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                String jolokiaResponse = IOUtils.toString(
                        this.getClass().getResourceAsStream("/" + metricName + "/expectedJolokiaResponse-body.json"),
                        "UTF-8"
                );
                exchange.getIn().setBody(jolokiaResponse);
            }
        });
        ProducerTemplate pt = context.createProducerTemplate();
        pt.sendBody("direct:" + metricName, "hello");
        Thread.sleep(2000);
        JSONAssert.assertEquals((String)jolokiaMock.getReceivedExchanges().get(0).getIn().getBody(), jolokiaRequest, JSONCompareMode.LENIENT);
        JSONAssert.assertEquals((String)hawkularMock.getReceivedExchanges().get(0).getIn().getBody(), hawkularRequest, JSONCompareMode.LENIENT);
    }

}
