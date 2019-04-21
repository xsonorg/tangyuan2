package org.xson.tangyuan.sharding;

import org.xson.tangyuan.ognl.vars.Variable;

public class HashShardingHandler extends AbstractShardingHandler {

	@Override
	public ShardingResult selectDataSourceAndTable(ShardingDefVo defVo, ShardingArgVo argVo, Object arg) {
		Variable[] keywords = argVo.getKeywords();
		if (null == keywords) {
			keywords = defVo.getKeywords();
		}
		// int hashCode = getHashCode(keywords[0], arg);
		// fix bug -x
		int hashCode = Math.abs(getHashCode(keywords[0], arg));

		// 按照Hash划分, 1.hash mod dbCount, 2, hash mode talbeCount
		// long tableIndex = hashCode % defVo.getTableCount();
		// long dbIndex = hashCode % defVo.getDbCount();
		// if (defVo.isTableNameIndexIncrement()) {
		// tableIndex = tableIndex + (dbIndex * defVo.getTableCount());
		// }

		// fix bug with TableNameIndexIncrement
		// long dbIndex = -1;
		// long tableIndex = -1;
		// if (defVo.isTableNameIndexIncrement()) {
		// int allTable = defVo.getDbCount() * defVo.getTableCount();
		// tableIndex = hashCode % allTable;
		// dbIndex = tableIndex / defVo.getTableCount();
		// } else {
		// // 按照Hash划分, 1.hash mod dbCount, 2, hash mode talbeCount
		// dbIndex = hashCode % defVo.getDbCount();
		// tableIndex = hashCode % defVo.getTableCount();
		// }
		// if (-1 == dbIndex || -1 == tableIndex) {
		// throw new ShardingException("分库分表索引不合法");
		// }

		// int allTable = defVo.getDbCount() * defVo.getTableCount();
		// long dbIndex = hashCode % allTable;
		// long tableIndex = tableIndex = hashCode % allTable;

		int allTable = defVo.getDbCount() * defVo.getTableCount();
		long tableIndex = hashCode % allTable;
		long dbIndex = tableIndex / defVo.getTableCount();
		if (!defVo.isTableNameIndexIncrement()) {
			tableIndex = tableIndex - (dbIndex * defVo.getTableCount());
		}

		return getResult(tableIndex, dbIndex, defVo, argVo);
	}

	private int getHashCode(Variable varVo, Object arg) {
		Object value = varVo.getValue(arg);
		if (null == value) {
			throw new ShardingException("分库分表对象值为空");
		}
		return value.hashCode();
	}

	public static void main(String[] args) {
		System.out.println(2 % 0);
	}
}
