package com.github.emalock3.camel.jira;

import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.atlassian.jira.rest.client.domain.Version;

public class JiraProjectVersionComponentTest extends CamelTestSupport {
	
	private static volatile StaticJsonFileServer SERVER;
	
	@BeforeClass
	public static void beforeClass() throws IOException, URISyntaxException {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		SERVER = new StaticJsonFileServer("project-version.json");
		SERVER.start();
	}
	
	@AfterClass
	public static void afterClass() {
		SERVER.shutdown();
		SERVER = null;
	}
	
	@EndpointInject(uri = "mock:result")
	protected MockEndpoint resultEndpoint;
	
	@Test
	public void test() {
		template.sendBody("direct:start", "hoge11");
		List<Exchange> exchanges = resultEndpoint.getReceivedExchanges();
		assertThat(exchanges.size(), is(1));
		Exchange exchange = exchanges.get(0);
		assertThat(exchange.getIn().getBody(List.class).size(), is(68));
		assertThat(exchange.getIn().getBody(List.class).get(0).getClass().getName(), 
				is(Version.class.getName()));
	}
	
	@Override
	protected RouteBuilder createRouteBuilder() {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				from("direct:start")
					.toF("jira-project-version:CAMEL?baseURI=http://localhost:%d/jira", 
							SERVER.port())
					.to("mock:result")
					.log("end");
			}
		};
	}

}
