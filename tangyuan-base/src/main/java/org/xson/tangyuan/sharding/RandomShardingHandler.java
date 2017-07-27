package org.xson.tangyuan.sharding;

public class RandomShardingHandler extends AbstractShardingHandler {

	@Override
	public ShardingResult selectDataSourceAndTable(ShardingDefVo defVo, ShardingArgVo argVo, Object arg) {
		// 这种情况下不需要参数
		long tableIndex = (int) (Math.random() * defVo.getTableCount());
		int dbIndex = (int) (Math.random() * defVo.getDbCount());
		if (defVo.isTableNameIndexIncrement()) {
			tableIndex = tableIndex + (dbIndex * defVo.getTableCount());
		}
		return getResult(tableIndex, dbIndex, defVo, argVo);
	}

}
