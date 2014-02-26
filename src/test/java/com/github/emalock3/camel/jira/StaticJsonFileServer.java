package com.github.emalock3.camel.jira;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import org.glassfish.grizzly.PortRange;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.ContentType;

public class StaticJsonFileServer {
	private final AtomicReference<HttpServer> serverRef = new AtomicReference<>();
	private final Path resourcePath;
	
	public StaticJsonFileServer(String resourceName) {
		try {
			this.resourcePath = 
					new File(getClass().getResource(resourceName).toURI()).toPath();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void start() throws IOException {
		HttpServer server = HttpServer.createSimpleServer(".", new PortRange(48080, 58080));
		server.getServerConfiguration().addHttpHandler(new HttpHandler() {
			private final ContentType contentType = 
					ContentType.newContentType("application/json", "UTF-8");
			@Override
			public void service(Request request, Response response) throws Exception {
				byte[] jsonBytes = Files.readAllBytes(resourcePath);
				response.setContentType(contentType);
				response.setContentLength(jsonBytes.length);
				response.getOutputStream().write(jsonBytes);
			}
		}, "/*");
		server.start();
		serverRef.set(server);
	}
	
	public int port() {
		HttpServer server = serverRef.get();
		if (server != null) {
			return server.getListener("grizzly").getPort();
		}
		return -1;
	}
	
	public void shutdown() {
		HttpServer server = serverRef.get();
		if (server != null) {
			server.shutdown();
		}
	}
}
