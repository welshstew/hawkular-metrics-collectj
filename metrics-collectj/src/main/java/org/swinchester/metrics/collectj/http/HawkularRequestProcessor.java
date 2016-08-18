package org.swinchester.metrics.collectj.http;

import okhttp3.*;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.swinchester.metrics.collectj.Constants;

/**
 * Created by wisyzi on 17/08/2016.
 */
public class HawkularRequestProcessor implements Processor {

    OkHttpClient client;

    @Override
    public void process(Exchange exchange) throws Exception {

        if(client == null){
            client = OkHttpUtils.getUnsafeOkHttpClient();
        }
        String hawkularMetricsHost = exchange.getProperty(Constants.HAWKULAR_METRICS_HOST).toString();
        String hawkularMetricsType = exchange.getProperty(Constants.HAWKULAR_METRICS_TYPE).toString();
        String hawkularMetricsUrl = hawkularMetricsHost + "/hawkular/metrics/" + hawkularMetricsType + "/data";

        MediaType mediaType = MediaType.parse(Constants.APPLICATION_JSON);
        RequestBody body = RequestBody.create(mediaType, (String) exchange.getIn().getBody());
        Request request = new Request.Builder()
                .url(hawkularMetricsUrl)
                .post(body)
                .addHeader(Constants.AUTHORIZATION, (String) exchange.getProperty(Constants.AUTHORIZATION))
                .addHeader(Constants.HAWKULAR_TENANT, (String) exchange.getProperty(Constants.HAWKULAR_TENANT))
                .addHeader(Exchange.CONTENT_TYPE, Constants.APPLICATION_JSON)
                .build();

        Response response = client.newCall(request).execute();
        exchange.getIn().setBody(response.body().string());
    }
}
