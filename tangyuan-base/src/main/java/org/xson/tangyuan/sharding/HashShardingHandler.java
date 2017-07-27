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
		long tableIndex = hashCode % defVo.getTableCount();
		long dbIndex = hashCode % defVo.getDbCount();
		if (defVo.isTableNameIndexIncrement()) {
			tableIndex = tableIndex + (dbIndex * defVo.getTableCount());
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
