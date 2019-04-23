package org.xson.tangyuan.rpc.balance;

/**
 * balance 处理器
 */
public interface BalanceHandler {

	public void init(BalanceVo balanceVo);

	public BalanceHostVo select(BalanceVo balanceVo);

}
