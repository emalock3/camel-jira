package com.github.emalock3.camel.jira.converter;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.camel.Converter;

@Converter
public class URITypeConverter {
	
	@Converter
	public static URI toURI(String target) throws URISyntaxException {
		return new URI(target);
	}
}
