package org.xson.tangyuan.rpc.client;

import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.rpc.RpcException;
import org.xson.tangyuan.rpc.xml.RpcClientVo;
import org.xson.tangyuan.util.PlaceholderResourceSupport;
import org.xson.tangyuan.util.Resources;
import org.xson.tools.httpclient.HttpClientFactory;
import org.xson.tools.httpclient.HttpClientVo;
import org.xson.tools.httpclient.XHttpClient;

public class HttpRpcClient extends AbstractRpcClient {

	private HttpClientFactory factory = null;

	public HttpRpcClient(RpcClientVo rpcClientVo) throws Throwable {
		this.rpcClientVo = rpcClientVo;
	}

	private String sendPostRequest(String url, byte[] buffer, String contentType) throws Throwable {
		XHttpClient xHttpClient = this.factory.getPoolingHttpClient();
		// CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			URI uri = new URI(url);
			HttpPost httpost = new HttpPost(uri);
			ByteArrayEntity byteArrayEntity = new ByteArrayEntity(buffer, ContentType.create(contentType, "UTF-8"));
			httpost.setEntity(byteArrayEntity);
			CloseableHttpResponse response = (CloseableHttpResponse) xHttpClient.execute(httpost);
			try {
				int status = response.getStatusLine().getStatusCode();
				if (status != 200) {
					throw new RpcException("Unexpected response status: " + status);
				}
				HttpEntity entity = response.getEntity();
				return EntityUtils.toString(entity, "UTF-8");
			} finally {
				response.close();
			}
		} finally {
			xHttpClient.close();
		}
	}

	@Override
	public XCO call(String url, XCO request) throws Throwable {
		String xml = request.toXMLString();
		// x-application/xco
		String result = sendPostRequest(url, xml.getBytes("UTF-8"), "application/xco");
		return XCO.fromXML(result);
	}

	@Override
	public void init() throws Throwable {
		HttpClientVo vo = null;
		String resource = this.rpcClientVo.getResource();
		if (null != resource) {
			Properties properties = new Properties();
			InputStream inputStream = Resources.getResourceAsStream(resource);
			inputStream = PlaceholderResourceSupport.processInputStream(inputStream,
					TangYuanContainer.getInstance().getXmlGlobalContext().getPlaceholderMap());
			properties.load(inputStream);
			inputStream.close();
			vo = new HttpClientVo(properties);
		}
		this.factory = new HttpClientFactory(vo, true);
	}

	@Override
	public void shutdown() {
		if (null != factory) {
			factory.shutdown();
		}
	}

}
