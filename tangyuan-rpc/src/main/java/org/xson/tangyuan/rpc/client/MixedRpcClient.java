package org.xson.tangyuan.rpc.client;

import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;

import org.xson.common.object.XCO;
import org.xson.tangyuan.rpc.RpcException;
import org.xson.tangyuan.rpc.xml.RemoteNodeVo;

// www.baidu.com/xxx/yyy			1
// http://www.baidu.com/xxx/yyy		2
// pigeon://www.baidu.com/xxx/yyy	3
// {aaaaaaaaaaa}/xxx/yyy			4

//public class MixedRpcClient implements TangYuanRpc {

public class MixedRpcClient extends AbstractRpcClient {

	// domain-->Client
	private Map<String, AbstractRpcClient>	rpcClientMap;
	// id-->remote-node
	private Map<String, RemoteNodeVo>		remoteNodeMap;

	private AbstractRpcClient				defaultClientRpc;

	private String							separator	= "/";

	public MixedRpcClient(Map<String, AbstractRpcClient> rpcClientMap, Map<String, RemoteNodeVo> remoteNodeMap) {
		this.rpcClientMap = rpcClientMap;
		this.remoteNodeMap = remoteNodeMap;
	}

	public MixedRpcClient(AbstractRpcClient defaultClientRpc) {
		this.defaultClientRpc = defaultClientRpc;
	}

	@Override
	public XCO call(String url, XCO request) throws Throwable {

		if (null != defaultClientRpc) {
			return call0(url, request);
		}

		// support 1,2,3,4

		boolean noSchema = true;
		String domain = null;
		if (url.indexOf("://") > -1) {
			URI uri = new URI(url);
			domain = uri.getHost();
			noSchema = false;
		} else {
			int beginIndex = url.indexOf(separator);
			if (url.startsWith("{")) {// {}
				int endIndex = url.indexOf("}");
				String remoteId = url.substring(1, endIndex);
				RemoteNodeVo remoteNodeVo = remoteNodeMap.get(remoteId);
				if (null == remoteNodeVo) {
					throw new RpcException("Illegal service URI: " + url);
				}
				domain = remoteNodeVo.getDomain();
				// 重新构建URL fixbug
				url = domain + url.substring(endIndex + 1);
			} else {
				domain = url.substring(0, beginIndex);
			}
		}
		AbstractRpcClient rpc = rpcClientMap.get(domain);
		if (null == rpc) {
			throw new RpcException("Illegal service URI: " + url);
		}
		if (noSchema) {
			url = rpc.getSchema() + "://" + url;
		}
		return rpc.call(url, request);
	}

	private XCO call0(String url, XCO request) throws Throwable {
		// support 1,2,3
		if (url.indexOf("://") < 0) {
			url = defaultClientRpc.getSchema() + "://" + url;
		}
		return defaultClientRpc.call(url, request);
	}

	@Override
	public void init() throws Throwable {
		// TODO Auto-generated method stub
	}

	@Override
	public void shutdown() {
		if (null != defaultClientRpc) {
			defaultClientRpc.shutdown();
		} else {
			for (Entry<String, AbstractRpcClient> entry : rpcClientMap.entrySet()) {
				entry.getValue().shutdown();
			}
		}
	}

}
