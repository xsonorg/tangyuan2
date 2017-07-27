package org.xson.tangyuan.sharding;

import org.xson.tangyuan.ognl.vars.Variable;

public class RangeShardingHandler extends AbstractShardingHandler {

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
		// 如果表名递增,则直接可以获取
		long tableIndex = (value - 1) / defVo.getTableCapacity();
		long dbIndex = (value - 1) / (defVo.getTableCount() * defVo.getTableCapacity());
		if (!defVo.isTableNameIndexIncrement()) {
			tableIndex = tableIndex - (dbIndex * defVo.getTableCount()); // 这是对的
		}
		return getResult(tableIndex, dbIndex, defVo, argVo);
	}

}
