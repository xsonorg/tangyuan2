package org.xson.tangyuan.client.http;

import java.io.IOException;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class XPoolingHttpClient extends AbstractHttpClient {

	private HttpClientBuilder                  builder = null;

	private PoolingHttpClientConnectionManager cm      = null;

	protected XPoolingHttpClient(HttpClientBuilder builder, PoolingHttpClientConnectionManager cm) {
		this.builder = builder;
		this.cm = cm;
	}

	@Override
	public HttpClient getHttpClient() {
		return this.builder.build();
	}

	@Override
	public void close() throws IOException {
	}

	public synchronized void shutdown() {
		if (null == this.cm) {
			return;
		}
		this.cm.shutdown();
		this.cm = null;
		this.builder = null;
	}

}
