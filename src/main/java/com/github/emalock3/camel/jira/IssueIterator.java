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

import java.util.Iterator;

import com.atlassian.jira.rest.client.IssueRestClient;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.ProgressMonitor;
import com.atlassian.jira.rest.client.SearchRestClient;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.SearchResult;

class IssueIterator implements Iterator<Issue> {
	
	static final int DEFAULT_BUFFER_SIZE = 50;
	
	private final SearchRestClient searchClient;
	private final IssueRestClient issueClient;
	private final ProgressMonitor monitor = new NullProgressMonitor();
	private final String jql;
	private final int bufferSize;
	private Iterator<BasicIssue> currentIterator;
	private int currentPos = 0;
	private int total;
	
	IssueIterator(JiraRestClient client, String projectKey) {
		this(client, projectKey, null, DEFAULT_BUFFER_SIZE);
	}
	
	IssueIterator(JiraRestClient client, String projectKey, String fixVersionId) {
		this(client, projectKey, fixVersionId, DEFAULT_BUFFER_SIZE);
	}
	
	IssueIterator(JiraRestClient client, String projectKey, String fixVersionId, int bufferSize) {
		searchClient = client.getSearchClient();
		issueClient = client.getIssueClient();
		this.bufferSize = bufferSize;
		if (fixVersionId == null || fixVersionId.isEmpty()) {
			jql = String.format(
					"project = %s order by updated desc", projectKey);
		} else {
			jql = String.format("project = %s AND fixVersion = %s order by updated desc", 
					projectKey, fixVersionId);
		}
		updateSearchResult();
	}
	
	private synchronized void updateSearchResult() {
		SearchResult searchResult = searchClient.searchJql(jql, bufferSize, currentPos, monitor);
		total = searchResult.getTotal();
		currentIterator = searchResult.getIssues().iterator();
	}
	
	@Override
	public synchronized boolean hasNext() {
		if (currentIterator.hasNext()) {
			return true;
		}
		if (total <= currentPos) {
			return false;
		}
		updateSearchResult();
		return currentIterator.hasNext();
	}

	@Override
	public synchronized Issue next() {
		BasicIssue basicIssue = currentIterator.next();
		currentPos++;
		return issueClient.getIssue(basicIssue.getKey(), monitor);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
