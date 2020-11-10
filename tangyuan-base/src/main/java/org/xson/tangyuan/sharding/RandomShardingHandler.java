package org.xson.tangyuan.sharding;

public class RandomShardingHandler extends AbstractShardingHandler {

	@Override
	public ShardingResult selectDataSourceAndTable(ShardingDefVo defVo, ShardingArgVo argVo, Object arg) {
		int allTable = defVo.getDbCount() * defVo.getTableCount();
		long tableIndex = (int) (Math.random() * allTable);
		long dbIndex = tableIndex / defVo.getTableCount();
		if (!defVo.isTableNameIndexIncrement()) {
			tableIndex = tableIndex - (dbIndex * defVo.getTableCount());
		}
		return getResult(tableIndex, dbIndex, defVo, argVo);
	}

}
