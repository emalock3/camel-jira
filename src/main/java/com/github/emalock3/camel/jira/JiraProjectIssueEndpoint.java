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

import org.apache.camel.Component;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.spi.UriParam;

import com.atlassian.jira.rest.client.JiraRestClient;

/**
 * 
 */
@Data
@EqualsAndHashCode(callSuper=false)
class JiraProjectIssueEndpoint extends AbstractJiraEndpoint {
	
	@UriParam private String fixVersionId;
	@UriParam private Integer bufferSize;

	JiraProjectIssueEndpoint(String endpointUri, Component component) {
		super(endpointUri, component);
	}

	JiraProjectIssueEndpoint(String endpointUri, Component component, URI baseURI) {
		super(endpointUri, component, baseURI);
	}
	
	@Override
	public Producer createProducer() throws Exception {
		return new DefaultProducer(this) {
			public void process(Exchange exchange) throws Exception {
				Message in = exchange.getIn();
				JiraRestClient c = createClient();
				String p = getProject();
				if (fixVersionId == null || fixVersionId.isEmpty()) {
					in.setBody(new IssueIterator(c, p));
				} else if (bufferSize == null || bufferSize.intValue() < IssueIterator.DEFAULT_BUFFER_SIZE) {
					in.setBody(new IssueIterator(c, p, fixVersionId));
				} else {
					in.setBody(new IssueIterator(c, p, fixVersionId, bufferSize));
				}
			}
		};
	}

}
