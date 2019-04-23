package org.xson.tangyuan.rpc.balance;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * 负载均衡管理
 */
public class BalanceManager {

	private static BalanceManager instance = new BalanceManager();

	public static BalanceManager getInstance() {
		return instance;
	}

	private boolean					balance		= false;

	private Map<String, BalanceVo>	balanceMap	= null;	// new HashMap<>();

	private BalanceManager() {

	}

	public void setBalanceMap(Map<String, BalanceVo> balanceMap) {
		this.balanceMap = balanceMap;
		this.balance = true;
	}

	public BalanceHostVo select(String url) throws URISyntaxException {
		if (!balance) {
			return null;
		}
		URI uri = new URI(url);
		String domain = uri.getHost();
		BalanceVo balanceVo = balanceMap.get(domain);
		if (null == balanceVo) {
			return null;
		}
		return balanceVo.select();
	}
}
