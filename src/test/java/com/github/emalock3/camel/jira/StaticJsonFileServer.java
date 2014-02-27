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
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.http.util.ContentType;

public class StaticJsonFileServer {
	
	public static class HandleResource {
		private final Path resourcePath;
		private final String[] mapping;
		HandleResource(String resourceName, String... mapping) {
			try {
				this.resourcePath = 
						new File(getClass().getResource(resourceName).toURI()).toPath();
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
			this.mapping = mapping;
		}
		public Path getResourcePath() {
			return resourcePath;
		}
		public String[] getMapping() {
			return mapping;
		}
	}
	
	public static HandleResource from(String resourceName, String... mapping) {
		return new HandleResource(resourceName, mapping);
	}
	
	private final AtomicReference<HttpServer> serverRef = new AtomicReference<>();
	private final HandleResource[] handleResources;
	
	public StaticJsonFileServer(HandleResource... handleResources) {
		if (handleResources.length == 0) {
			throw new IllegalArgumentException("handleResources must not be empty.");
		}
		this.handleResources = handleResources;
	}
	
	public void start() throws IOException {
		HttpServer server = HttpServer.createSimpleServer(".", new PortRange(48080, 58080));
		ServerConfiguration sc = server.getServerConfiguration();
		for (final HandleResource hr : handleResources) {
			sc.addHttpHandler(new HttpHandler() {
				private final ContentType contentType = 
						ContentType.newContentType("application/json", "UTF-8");
				public void service(Request request, Response response) throws Exception {
					byte[] jsonBytes = Files.readAllBytes(hr.getResourcePath());
					response.setContentType(contentType);
					response.setContentLength(jsonBytes.length);
					response.getOutputStream().write(jsonBytes);
				}
			}, hr.getMapping());
		}
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
