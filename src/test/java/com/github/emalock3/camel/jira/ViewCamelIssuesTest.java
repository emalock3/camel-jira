package com.github.emalock3.camel.jira;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;

public class ViewCamelIssuesTest {

	public static void main(String[] args) throws Exception {
		Main main = new Main();
		main.addRouteBuilder(new RouteBuilder() {
			public void configure() throws Exception {
				from("direct:start")
					.to("jira-project-issue:CAMEL?baseURI=https://issues.apache.org/jira&fixVersionId=12315681")
					.split(body()).streaming()
						.log("${body.key} ${body.status} ${body.updateDate}")
					.end()
					.log("end");
			}
		});
		main.start();
		main.getCamelTemplate().sendBody("direct:start", "hoge");
		main.shutdown();
	}

}
