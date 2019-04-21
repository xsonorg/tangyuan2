package org.xson.tangyuan.sharding;

public class RandomShardingHandler extends AbstractShardingHandler {

	@Override
	public ShardingResult selectDataSourceAndTable(ShardingDefVo defVo, ShardingArgVo argVo, Object arg) {
		// 这种情况下不需要参数
		// long tableIndex = (int) (Math.random() * defVo.getTableCount());
		// int dbIndex = (int) (Math.random() * defVo.getDbCount());
		// if (defVo.isTableNameIndexIncrement()) {
		// tableIndex = tableIndex + (dbIndex * defVo.getTableCount());
		// }

		// long dbIndex = -1;
		// long tableIndex = -1;
		// if (defVo.isTableNameIndexIncrement()) {
		// int allTable = defVo.getDbCount() * defVo.getTableCount();
		// tableIndex = (int) (Math.random() * allTable);
		// dbIndex = tableIndex / defVo.getTableCount();
		// } else {
		// dbIndex = (int) (Math.random() * defVo.getDbCount());
		// tableIndex = (int) (Math.random() * defVo.getTableCount());
		// }
		//
		// if (-1 == dbIndex || -1 == tableIndex) {
		// throw new ShardingException("分库分表索引不合法");
		// }

		int allTable = defVo.getDbCount() * defVo.getTableCount();
		long tableIndex = (int) (Math.random() * allTable);
		long dbIndex = tableIndex / defVo.getTableCount();
		if (!defVo.isTableNameIndexIncrement()) {
			tableIndex = tableIndex - (dbIndex * defVo.getTableCount());
		}

		return getResult(tableIndex, dbIndex, defVo, argVo);
	}

}
