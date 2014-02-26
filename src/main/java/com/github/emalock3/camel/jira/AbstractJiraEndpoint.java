/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.emalock3.camel.jira;

import java.net.URI;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import org.apache.camel.Component;
import org.apache.camel.impl.ProcessorEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriPath;

import com.atlassian.jira.rest.client.AuthenticationHandler;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.ProgressMonitor;
import com.atlassian.jira.rest.client.auth.AnonymousAuthenticationHandler;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory;

@Data
@EqualsAndHashCode(callSuper=false)
public abstract class AbstractJiraEndpoint extends ProcessorEndpoint {
	
	@UriPath @NonNull private String project;
	
	/**
	 * Base URI
	 * ex: http://localhost:8080/jira
	 */
	@UriParam @NonNull private URI baseURI;
	
	/**
	 * username for JIRA authentication
	 */
	@UriParam private String username;
	
	/**
	 * password for JIRA authentication
	 */
	@UriParam private String password;
	
	public AbstractJiraEndpoint(String endpointUri, Component component) {
		super(endpointUri, component);
	}
	
	public AbstractJiraEndpoint(String endpointUri, Component component, URI baseURI) {
		super(endpointUri, component);
		setBaseURI(baseURI);
	}
	
	public JiraRestClient createClient() {
		JerseyJiraRestClientFactory factory = new JerseyJiraRestClientFactory();
		AuthenticationHandler authenticationHandler;
		if (getUsername() != null && getPassword() != null) {
			authenticationHandler = new BasicHttpAuthenticationHandler(getUsername(), getPassword());
		} else {
			authenticationHandler = new AnonymousAuthenticationHandler();
		}
		return factory.create(getBaseURI(), authenticationHandler);
	}
	
	public ProgressMonitor defaultProgressMonitor() {
		return new NullProgressMonitor();
	}

}
