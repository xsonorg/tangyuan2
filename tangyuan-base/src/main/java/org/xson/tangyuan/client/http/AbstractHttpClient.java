package org.xson.tangyuan.client.http;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;

public abstract class AbstractHttpClient implements XHttpClient {

	public String defaultCharset = "UTF-8";

	@Override
	public HttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException {
		CloseableHttpClient httpclient = (CloseableHttpClient) getHttpClient();
		CloseableHttpResponse response = httpclient.execute(request);
		return response;
	}

}
