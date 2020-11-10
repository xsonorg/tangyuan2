package org.xson.tangyuan.rpc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TangYuanRpcPlaceHolderHandler implements RpcPlaceHolderHandler {

	private Map<String, Integer>	placeHolderRemoteNodeMap	= null;

	private Map<String, String>		serviceURLMap				= new ConcurrentHashMap<String, String>();

	private String					separator					= "/";

	public TangYuanRpcPlaceHolderHandler(Map<String, Integer> placeHolderRemoteNodeMap) {
		this.placeHolderRemoteNodeMap = placeHolderRemoteNodeMap;
	}

	/**
	 * 解析 {xxx}/a/b
	 */
	@Override
	public String parse(String serviceURL) {

		String serviceLocalURL = serviceURLMap.get(serviceURL);
		if (null != serviceLocalURL) {
			return serviceLocalURL;
		}

		if (serviceURL.startsWith("{")) {// {}
			int endIndex = serviceURL.indexOf("}");
			String remoteId = serviceURL.substring(1, endIndex);
			if (placeHolderRemoteNodeMap.containsKey(remoteId)) {
				int startIndex = serviceURL.indexOf(separator, endIndex);
				if (startIndex > 0) {
					serviceLocalURL = serviceURL.substring(startIndex + 1);
					serviceURLMap.put(serviceURL, serviceLocalURL);
					return serviceLocalURL;
				}
			}
		}
		return null;
	}

}
