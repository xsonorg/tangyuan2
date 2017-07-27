package org.xson.tangyuan.rpc.client;

import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.xson.common.object.XCO;
import org.xson.tangyuan.rpc.xml.RpcClientVo;

public class HttpRpcClient extends AbstractClientRpc {

	public HttpRpcClient(RpcClientVo rpcClientVo) {
		this.rpcClientVo = rpcClientVo;
	}

	private String sendPostRequest(String url, byte[] buffer, String header) throws Throwable {
		// TODO: 默认值设置
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			URI uri = new URI(url);
			HttpPost httpost = new HttpPost(uri);
			ByteArrayEntity byteArrayEntity = new ByteArrayEntity(buffer, ContentType.create(header, "UTF-8"));
			httpost.setEntity(byteArrayEntity);
			CloseableHttpResponse response = httpclient.execute(httpost);
			try {
				int status = response.getStatusLine().getStatusCode();
				if (status != 200) {
					throw new Exception("Unexpected response status: " + status);
				}
				HttpEntity entity = response.getEntity();
				return EntityUtils.toString(entity, "UTF-8");
			} finally {
				response.close();
			}
		} finally {
			httpclient.close();
		}
	}

	@Override
	public XCO call(String url, XCO request) throws Throwable {
		String xml = request.toXMLString();
		// TODO: x-application/xco
		String result = sendPostRequest(url, xml.getBytes("UTF-8"), "x-application/xco");
		return XCO.fromXML(result);
	}

}
