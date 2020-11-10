package org.xson.tangyuan.rpc.client;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.xson.common.object.XCO;
import org.xson.tangyuan.client.http.XHttpClient;
import org.xson.tangyuan.rpc.RpcException;
import org.xson.tangyuan.rpc.balance.BalanceManager;
import org.xson.tangyuan.rpc.host.RemoteHostManager;
import org.xson.tangyuan.rpc.xml.vo.RpcClientVo;

public class HttpRpcClient extends AbstractRpcClient {

	private XHttpClient       xHttpClient       = null;
	private BalanceManager    balanceManager    = null;
	private RemoteHostManager remoteHostManager = null;

	public HttpRpcClient(RpcClientVo rpcClientVo, XHttpClient xHttpClient, BalanceManager balanceManager, RemoteHostManager remoteHostManager) {
		this.rpcClientVo = rpcClientVo;
		this.xHttpClient = xHttpClient;
		this.balanceManager = balanceManager;
		this.remoteHostManager = remoteHostManager;
	}

	private String sendPostRequest(String url, byte[] buffer, String contentType) throws Throwable {
		try {
			URI             uri             = new URI(url);
			HttpPost        httpost         = new HttpPost(uri);
			ByteArrayEntity byteArrayEntity = new ByteArrayEntity(buffer, ContentType.create(contentType, StandardCharsets.UTF_8));
			httpost.setEntity(byteArrayEntity);
			CloseableHttpResponse response = (CloseableHttpResponse) xHttpClient.execute(httpost);
			try {
				int status = response.getStatusLine().getStatusCode();
				if (status != 200) {
					throw new RpcException("Unexpected response status: " + status);
				}
				HttpEntity entity = response.getEntity();
				return EntityUtils.toString(entity, StandardCharsets.UTF_8);
			} finally {
				response.close();
			}
		} finally {
			xHttpClient.close();
		}
	}

	@Override
	public Object call(String url, Object arg, Object attachment) throws Throwable {
		// url = doBalance(url);
		url = parseURL(url);
		String xml    = arg.toString();
		// x-application/xco,x-application/xco-xml
		// 后期考虑xson序列化方式，可以通过contentType进行判断
		String result = sendPostRequest(url, xml.getBytes("UTF-8"), "application/xco");
		return XCO.fromXML(result);
	}

	@Override
	public void init() throws Throwable {
	}

	@Override
	public void shutdown() {
		this.xHttpClient = null;
		this.balanceManager = null;
		this.remoteHostManager = null;
	}

	private String parseURL(String url) throws Throwable {
		if (null != this.balanceManager) {
			url = (String) this.balanceManager.select(url, "http");
		}
		if (null != this.remoteHostManager) {
			url = (String) this.remoteHostManager.parse(url, "http");
		}
		return url;
	}

	//////////////////////////////////////////////////////////////////////////

	//this.xHttpClient = HttpClientManager.getInstance().getXHttpClient(this.rpcClientVo.getUsi());
	//this.balanceManager = BalanceManager.getInstance();

	//		String newDomain = this.hostMap.get(domain);
	//		if (null == newDomain) {
	//			return url;
	//		}
	//		int pos = url.indexOf(domain);
	//		url = url.substring(0, pos) + newDomain + url.substring(pos + domain.length());
	//		return url;
	//	private String replaceDomain(String url) throws Throwable {
	//	if (null == this.remoteHostManager) {
	//		return url;
	//	}
	//	return this.remoteHostManager.parse(url);
	//}
	//	private String doBalance(String url) throws Throwable {
	//		if (null == this.balanceManager) {
	//			return url;
	//		}
	//		BalanceHostVo hostVo = this.balanceManager.select(url);
	//		if (null == hostVo) {
	//			return url;
	//		}
	//		// 替换新的Domain
	//		URI    uri    = new URI(url);
	//		String domain = uri.getHost();
	//		int    pos    = url.indexOf(domain);
	//		url = url.substring(0, pos) + hostVo.getDomain() + url.substring(pos + domain.length());
	//		// System.err.println("Balance:" + url);
	//		return url;
	//	}
	//	public HttpRpcClient(RpcClientVo rpcClientVo) throws Throwable {
	//		this.rpcClientVo = rpcClientVo;
	//	}
	//	//	@Override
	//	public XCO call(String url, XCO request) throws Throwable {
	//		url = doBalance(url);
	//		String xml    = request.toXMLString();
	//		// x-application/xco
	//		String result = sendPostRequest(url, xml.getBytes("UTF-8"), "application/xco");
	//		return XCO.fromXML(result);
	//	}
}
