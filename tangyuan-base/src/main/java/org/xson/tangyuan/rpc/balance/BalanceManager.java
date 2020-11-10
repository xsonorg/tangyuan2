package org.xson.tangyuan.rpc.balance;

import java.net.URI;
import java.util.Map;

import org.xson.tangyuan.util.CollectionUtils;

/**
 * 负载均衡管理
 */
public class BalanceManager {

	private Map<String, BalanceVo> balanceMap = null;

	public BalanceManager(Map<String, BalanceVo> balanceMap) {
		this.balanceMap = balanceMap;
	}

	public Object select(String url, String type) throws Throwable {
		if (CollectionUtils.isEmpty(this.balanceMap)) {
			return url;
		}

		// 后期可以支持TCP

		URI uri = new URI(url);
		String domain = uri.getHost();
		BalanceVo balanceVo = this.balanceMap.get(domain);
		if (null == balanceVo) {
			return url;
		}
		BalanceHostVo hostVo = balanceVo.select();
		if (null == hostVo) {
			return url;
		}

		// 对老端口的兼容
		if (uri.getPort() != -1) {
			domain = domain + ":" + uri.getPort();
		}

		// 对新端口的兼容
		String newDomain = hostVo.getDomain();
		if (hostVo.getPort() != -1) {
			newDomain = newDomain + ":" + hostVo.getPort();
		}

		int pos = url.indexOf(domain);
		url = url.substring(0, pos) + newDomain + url.substring(pos + domain.length());
		return url;
	}

}
