package com.github.emalock3.camel.jira;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.atlassian.jira.rest.client.AuthenticationHandler;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.auth.AnonymousAuthenticationHandler;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory;

public class IssueIteratorTest {
	
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

	@Test
	public void test() throws URISyntaxException {
		JerseyJiraRestClientFactory factory = new JerseyJiraRestClientFactory();
		AuthenticationHandler authenticationHandler = new AnonymousAuthenticationHandler();
		JiraRestClient restClient = factory.create(new URI(
				String.format("http://localhost:%d/jira", SERVER.port())), authenticationHandler);
		IssueIterator issueIterator = new IssueIterator(restClient, "CAMEL", "12315679");
		assertThat(issueIterator.hasNext(), is(true));
		while (issueIterator.hasNext()) {
			Issue issue = issueIterator.next();
			assertThat(issue.getKey(), is("CAMEL-7253"));
		}
	}

}
