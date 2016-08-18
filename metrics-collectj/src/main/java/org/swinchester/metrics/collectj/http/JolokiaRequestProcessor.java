package org.swinchester.metrics.collectj.http;

import okhttp3.*;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.swinchester.metrics.collectj.Constants;

/**
 * Created by wisyzi on 17/08/2016.
 */
public class JolokiaRequestProcessor implements Processor {

    OkHttpClient client;

    @Override
    public void process(Exchange exchange) throws Exception {

        if(client == null){
            client = OkHttpUtils.getUnsafeOkHttpClient();
        }
        String jolokiaHost = exchange.getProperty(Constants.JOLOKIA_API_HOST).toString();
        String jolokiaAggregateUrl = jolokiaHost + "/jolokia/aggregate";

        MediaType mediaType = MediaType.parse(Constants.APPLICATION_JSON);
        RequestBody body = RequestBody.create(mediaType, (String) exchange.getIn().getBody());
        Request request = new Request.Builder()
                .url(jolokiaAggregateUrl)
                .post(body)
                .addHeader(Constants.AUTHORIZATION, (String) exchange.getProperty(Constants.AUTHORIZATION))
                .addHeader(Exchange.CONTENT_TYPE, Constants.APPLICATION_JSON)
                .addHeader(Constants.KUBE_NAMESPACE, (String)exchange.getProperty(Constants.KUBE_NAMESPACE))
                .addHeader(Constants.KUBE_LABEL, (String)exchange.getProperty(Constants.KUBE_LABEL))
                .addHeader(Exchange.ACCEPT_CONTENT_TYPE, Constants.APPLICATION_JSON)
                .build();

        Response response = client.newCall(request).execute();
        exchange.getIn().setBody(response.body().string());
    }

}