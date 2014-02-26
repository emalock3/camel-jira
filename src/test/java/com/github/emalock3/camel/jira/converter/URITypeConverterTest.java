package com.github.emalock3.camel.jira.converter;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

public class URITypeConverterTest {

	@Test
	public void test() throws URISyntaxException {
		assertThat(URITypeConverter.toURI("http://test.com/"), is(new URI("http://test.com/")));
		try {
			URITypeConverter.toURI(":::");
			fail("':::' is not valid URI");
		} catch (URISyntaxException ignore) {
		}
	}

}
