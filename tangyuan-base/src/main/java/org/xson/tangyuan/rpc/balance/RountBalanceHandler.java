package org.xson.tangyuan.rpc.balance;

import java.util.List;

public class RountBalanceHandler implements BalanceHandler {

	// 不需要并发控制
	private int count = 0;

	@Override
	public void init(BalanceVo balanceVo) {
	}

	@Override
	public BalanceHostVo select(BalanceVo balanceVo) {
		List<BalanceHostVo> hostList = balanceVo.getHostList();
		int index = getCount() % hostList.size();
		return hostList.get(index);
	}

	public int getCount() {
		if (count == Integer.MAX_VALUE) {
			count = 0;
		}
		return count++;
	}

}
