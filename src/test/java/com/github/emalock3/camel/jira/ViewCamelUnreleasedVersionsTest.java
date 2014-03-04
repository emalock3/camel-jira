package com.github.emalock3.camel.jira;

import java.util.Comparator;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;

import com.atlassian.jira.rest.client.domain.Version;

public class ViewCamelUnreleasedVersionsTest {

	public static void main(String[] args) throws Exception {
		Main main = new Main();
		main.addRouteBuilder(new RouteBuilder() {
			public void configure() throws Exception {
				from("direct:start")
					.to("jira-project-version:CAMEL?baseURI=https://issues.apache.org/jira")
					.split(body())
						.filter(simple("${body.isReleased} == false && ${body.isArchived} == false")).log("${body.name}").end()
					.end()
					.sort(body(), new Comparator<Version>() {
						public int compare(Version o1, Version o2) {
							return o1.getName().compareTo(o2.getName());
						}
					})
					.log("end");
			}
		});
		main.start();
		main.getCamelTemplate().sendBody("direct:start", null);
		main.shutdown();
	}

}
