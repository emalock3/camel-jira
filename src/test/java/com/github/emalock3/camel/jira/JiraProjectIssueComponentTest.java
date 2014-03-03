package com.github.emalock3.camel.jira;

import static org.hamcrest.Matchers.is;

import java.util.Iterator;
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

import com.atlassian.jira.rest.client.domain.Issue;

public class JiraProjectIssueComponentTest extends CamelTestSupport {
	
	private static volatile StaticJsonFileServer SERVER;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		SERVER = new StaticJsonFileServer(
				StaticJsonFileServer.from("search-issues.json", "/jira/rest/api/latest/search"),
				StaticJsonFileServer.from("issue.json", "/jira/rest/api/latest/issue/*")
		);
		SERVER.start();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		SERVER.shutdown();
		SERVER = null;
	}
	
	@EndpointInject(uri = "mock:result")
	protected MockEndpoint resultEndpoint;

	@Test
	public void test() {
		template.sendBody("direct:start", "hoge");
		List<Exchange> exchanges = resultEndpoint.getReceivedExchanges();
		assertThat(exchanges.size(), is(1));
		Exchange exchange = exchanges.get(0);
		Iterator<Issue> iter = 
				exchange.getIn().getBody(IssueIterator.class);
		assertThat(iter.hasNext(), is(true));
		while (iter.hasNext()) {
			Issue issue = iter.next();
			assertThat(issue.getKey(), is("CAMEL-7253"));
		}
	}
	
	@Override
	protected RouteBuilder createRouteBuilder() {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				from("direct:start")
					.toF("jira-project-issue:CAMEL?baseURI=http://localhost:%d/jira", 
							SERVER.port())
					.to("mock:result")
					.log("end");
			}
		};
	}

}
