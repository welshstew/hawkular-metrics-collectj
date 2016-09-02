package org.swinchester.metrics.collectj.app;

import org.apache.camel.spring.boot.FatJarRouter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;

/**
 * Created by swinchester on 18/08/16.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"org.swinchester.metrics.collectj.routebuilder"})
@ImportResource({"META-INF/spring/camel-context.xml"})
public class CollectJApplication extends FatJarRouter{
}
