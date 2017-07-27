package org.xson.tangyuan.sharding;

import org.xson.tangyuan.ognl.vars.Variable;

public class ModShardingHandler extends AbstractShardingHandler {

	@Override
	public ShardingResult selectDataSourceAndTable(ShardingDefVo defVo, ShardingArgVo argVo, Object arg) {
		Variable[] keywords = argVo.getKeywords();
		if (null == keywords) {
			keywords = defVo.getKeywords();
		}
		long value = getValue(keywords[0].getValue(arg));
		if (value < 0) {
			throw new ShardingException("分库分表对象值非法: " + value);
		}
		// <!--按照mod划分, 1.key mod dbCount(dbCount > 1), 2, key mode talbeCount(tableCount > 1)-->
		long tableIndex = value % defVo.getTableCount();
		long dbIndex = value % defVo.getDbCount();
		if (defVo.isTableNameIndexIncrement()) {
			tableIndex = tableIndex + (dbIndex * defVo.getTableCount());
		}
		return getResult(tableIndex, dbIndex, defVo, argVo);
	}

}
