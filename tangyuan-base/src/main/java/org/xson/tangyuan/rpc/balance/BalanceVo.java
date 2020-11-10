package org.xson.tangyuan.rpc.balance;

import java.util.List;

public class BalanceVo {

	public enum Strategy {
		/** 轮循 */
		ROUND,
		/** 权重 */
		WEIGHT
	}

	private Strategy			strategy;

	private BalanceHandler		impl;

	private List<BalanceHostVo>	hostList;

	public BalanceVo(Strategy strategy, BalanceHandler impl, List<BalanceHostVo> hostList) {
		this.strategy = strategy;
		this.impl = impl;
		this.hostList = hostList;
		// init
		this.impl.init(this);
	}

	public BalanceHandler getImpl() {
		return impl;
	}

	public List<BalanceHostVo> getHostList() {
		return hostList;
	}

	public Strategy getStrategy() {
		return strategy;
	}

	public BalanceHostVo select() {
		return this.impl.select(this);
	}
}
