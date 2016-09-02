package org.swinchester.metrics.collectj.test;

import org.apache.camel.*;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.apache.camel.test.spring.CamelTestContextBootstrapper;
import org.apache.camel.test.spring.MockEndpointsAndSkip;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;

/**
 * Created by swinchester on 2/09/16.
 */
@RunWith(CamelSpringJUnit4ClassRunner.class)
@BootstrapWith(CamelTestContextBootstrapper.class)
@TestPropertySource(locations="classpath*:test.properties")
@MockEndpointsAndSkip("direct:call*")
@ContextConfiguration(locations = "classpath*:META-INF/spring/test-context.xml")
@PropertySource(value = "classpath*:test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class BrokerMemoryUnitTest {

    @Autowired
    protected CamelContext context;

    @EndpointInject(uri = "mock://direct:callJolokia")
    protected MockEndpoint jolokiaMock;

    @EndpointInject(uri = "mock://direct:callHawkular")
    protected MockEndpoint hawkularMock;

    @Test
    public void testMemoryMetrics() throws Exception {
        MetricsHelper.testMetric("brokermemory", (ModelCamelContext) context, jolokiaMock, hawkularMock);
    }

}
