package org.xson.tangyuan.rpc.client;

import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.rpc.xml.vo.RemoteNodeVo;

// www.baidu.com/xxx/yyy			1
// http://www.baidu.com/xxx/yyy		2
// pigeon://www.baidu.com/xxx/yyy	3
// {aaaaaaaaaaa}/xxx/yyy			4

public class MixedRpcClient extends AbstractRpcClient {

	// domain-->Client
	private Map<String, AbstractRpcClient>	rpcClientMap;
	// id-->remote-node
	private Map<String, RemoteNodeVo>		remoteNodeMap;

	private AbstractRpcClient				defaultClientRpc;

	private AbstractRpcClient				onlyClientRpc;

	private String							separator	= "/";

	public MixedRpcClient(Map<String, AbstractRpcClient> rpcClientMap, Map<String, RemoteNodeVo> remoteNodeMap, AbstractRpcClient defaultClientRpc) {
		this.rpcClientMap = rpcClientMap;
		this.remoteNodeMap = remoteNodeMap;
		this.defaultClientRpc = defaultClientRpc;
	}

	public MixedRpcClient(Map<String, AbstractRpcClient> rpcClientMap, AbstractRpcClient onlyClientRpc) {
		this.rpcClientMap = rpcClientMap;
		this.onlyClientRpc = onlyClientRpc;
	}

	@Override
	public void init() throws Throwable {
	}

	@Override
	public void shutdown() {
		if (null != this.rpcClientMap) {
			for (Entry<String, AbstractRpcClient> entry : rpcClientMap.entrySet()) {
				entry.getValue().shutdown();
			}
		}
	}

	@Override
	public Object call(String url, Object arg, Object attachment) throws Throwable {
		if (null != this.onlyClientRpc) {
			return call0(url, arg, attachment);
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
				// {aaaaaaaaaaa}/xxx/yyy
				int endIndex = url.indexOf("}");
				String remoteId = url.substring(1, endIndex);
				RemoteNodeVo remoteNodeVo = remoteNodeMap.get(remoteId);
				if (null == remoteNodeVo) {
					throw new TangYuanException(TangYuanLang.get("remote-node.id.invalid", remoteId, url));
				}
				domain = remoteNodeVo.getDomain();
				// 重新构建URL fixbug
				url = domain + url.substring(endIndex + 1);
			} else {
				// www.baidu.com/xxx/yyy
				domain = url.substring(0, beginIndex);
			}
		}
		AbstractRpcClient rpc = rpcClientMap.get(domain);
		if (null == rpc) {
			rpc = this.defaultClientRpc;
		}
		if (null == rpc) {
			throw new TangYuanException(TangYuanLang.get("remote-node.domain.invalid", domain, url));
		}
		if (noSchema) {
			url = rpc.getSchema() + "://" + url;
		}
		return rpc.call(url, arg, attachment);
	}

	private Object call0(String url, Object arg, Object attachment) throws Throwable {
		// support 1,2,3
		if (url.indexOf("://") < 0) {
			url = this.onlyClientRpc.getSchema() + "://" + url;
		}
		return this.onlyClientRpc.call(url, arg, attachment);
	}

}
