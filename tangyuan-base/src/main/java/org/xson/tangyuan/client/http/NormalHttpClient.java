package org.xson.tangyuan.client.http;

import java.io.IOException;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class NormalHttpClient extends AbstractHttpClient {

	private HttpClientBuilder builder;

	public NormalHttpClient(HttpClientBuilder builder) {
		this.builder = builder;
	}

	@Override
	public HttpClient getHttpClient() {
		return this.builder.build();
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public void shutdown() {
		builder = null;
	}

}
