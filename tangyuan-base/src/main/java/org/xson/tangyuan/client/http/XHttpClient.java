package org.xson.tangyuan.client.http;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;

public interface XHttpClient {

	public HttpClient getHttpClient();

	public void close() throws IOException;

	void shutdown();

	public HttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException;

}
