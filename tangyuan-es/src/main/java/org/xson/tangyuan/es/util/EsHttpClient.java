package org.xson.tangyuan.es.util;

import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.xson.tangyuan.client.http.HttpClientManager;
import org.xson.tangyuan.client.http.XHttpClient;
import org.xson.tangyuan.es.datasource.EsSourceVo;

public class EsHttpClient {

	//	private static Log  log            = LogFactory.getLog(EsHttpClient.class);

	private String      contentType    = "application/json; charset=UTF-8";
	private EsSourceVo  essvo          = null;
	private XHttpClient xHttpClient    = null;
	private String      defaultCharset = "UTF-8";

	public EsHttpClient(EsSourceVo essvo) {
		this.essvo = essvo;
	}

	public EsHttpResultWrapper get(String url) throws Throwable {
		try {
			URI     uri     = new URI(url);
			HttpGet httpGet = new HttpGet(uri);
			httpGet.addHeader(HTTP.CONTENT_TYPE, contentType);
			CloseableHttpResponse response = (CloseableHttpResponse) this.xHttpClient.execute(httpGet);
			try {
				return getResponseString(response);
			} finally {
				response.close();
			}
		} finally {
			this.xHttpClient.close();
		}
	}

	public EsHttpResultWrapper delete(String url) throws Throwable {
		try {
			URI        uri     = new URI(url);
			HttpDelete httpGet = new HttpDelete(uri);
			httpGet.addHeader(HTTP.CONTENT_TYPE, contentType);
			CloseableHttpResponse response = (CloseableHttpResponse) this.xHttpClient.execute(httpGet);
			try {
				return getResponseString(response);
			} finally {
				response.close();
			}
		} finally {
			this.xHttpClient.close();
		}
	}

	public EsHttpResultWrapper post(String url, String body) throws Throwable {
		try {
			URI          uri     = new URI(url);
			HttpPost     httpost = new HttpPost(uri);
			StringEntity entity  = new StringEntity(body, ContentType.APPLICATION_JSON);
			httpost.setEntity(entity);
			CloseableHttpResponse response = (CloseableHttpResponse) this.xHttpClient.execute(httpost);
			try {
				return getResponseString(response);
			} finally {
				response.close();
			}
		} finally {
			this.xHttpClient.close();
		}
	}

	public EsHttpResultWrapper put(String url, String body) throws Throwable {
		try {
			URI          uri     = new URI(url);
			HttpPut      httpost = new HttpPut(uri);
			StringEntity entity  = new StringEntity(body, ContentType.APPLICATION_JSON);
			httpost.setEntity(entity);
			CloseableHttpResponse response = (CloseableHttpResponse) this.xHttpClient.execute(httpost);
			try {
				return getResponseString(response);
			} finally {
				response.close();
			}
		} finally {
			this.xHttpClient.close();
		}
	}

	private EsHttpResultWrapper getResponseString(CloseableHttpResponse response) throws Throwable {
		//		int status = response.getStatusLine().getStatusCode();
		//		if (status != 200) {
		//			printResponseError(response);
		//			throw new ServiceException("Unexpected response status: " + status);
		//		}
		//		HttpEntity entity = response.getEntity();
		//		return EntityUtils.toString(entity, defaultCharset);

		int        status  = response.getStatusLine().getStatusCode();
		HttpEntity entity  = response.getEntity();
		String     content = EntityUtils.toString(entity, defaultCharset);
		return new EsHttpResultWrapper(content, status);
	}

	//	private String getResponseString201(CloseableHttpResponse response) throws Throwable {
	//		int status = response.getStatusLine().getStatusCode();
	//		if (!(status == 200 || status == 201)) {
	//			printResponseError(response);
	//			throw new ServiceException("Unexpected response status: " + status);
	//		}
	//		HttpEntity entity = response.getEntity();
	//		return EntityUtils.toString(entity, defaultCharset);
	//	}
	//	private void printResponseError(CloseableHttpResponse response) {
	//		try {
	//			HttpEntity entity    = response.getEntity();
	//			String     errorInfo = EntityUtils.toString(entity, defaultCharset);
	//			log.error(errorInfo);
	//		} catch (Throwable e) {
	//		}
	//	}

	public void init() throws Throwable {
		this.xHttpClient = HttpClientManager.getInstance().getXHttpClient(this.essvo.getUsi());
	}

	public void shutdown() {
		this.xHttpClient = null;
	}

}
