package org.xson.tangyuan.rpc.balance;

import java.util.List;

public class WeightBalanceHandler implements BalanceHandler {

	// 不需要并发控制
	private int		count		= 0;

	// 索引数组
	private int[]	indexArray	= null;

	@Override
	public void init(BalanceVo balanceVo) {
		List<BalanceHostVo> hostList = balanceVo.getHostList();
		int length = 0;
		for (int i = 0; i < hostList.size(); i++) {
			BalanceHostVo hostVo = hostList.get(i);
			length += hostVo.getWeight();
		}
		indexArray = new int[length];
		int index = 0;
		for (int i = 0; i < hostList.size(); i++) {
			BalanceHostVo hostVo = hostList.get(i);
			int weight = hostVo.getWeight();
			for (int j = 0; j < weight; j++) {
				indexArray[index++] = i;
			}
		}
	}

	// @Override
	// public String select(BalanceVo balanceVo) {
	// List<BalanceHostVo> hostList = balanceVo.getHostList();
	// int index = getCount() % indexArray.length;
	// return hostList.get(index).getDomain();
	// }

	@Override
	public BalanceHostVo select(BalanceVo balanceVo) {
		List<BalanceHostVo> hostList = balanceVo.getHostList();
		int index = getCount() % indexArray.length;
		index = indexArray[index];
		return hostList.get(index);
	}

	public int getCount() {
		if (count == Integer.MAX_VALUE) {
			count = 0;
		}
		return count++;
	}

}
