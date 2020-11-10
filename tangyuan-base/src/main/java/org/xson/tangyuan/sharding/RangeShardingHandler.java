package org.xson.tangyuan.sharding;

import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.ognl.vars.Variable;

public class RangeShardingHandler extends AbstractShardingHandler {

	@Override
	public ShardingResult selectDataSourceAndTable(ShardingDefVo defVo, ShardingArgVo argVo, Object arg) {
		Variable[] keywords = argVo.getKeywords();
		if (null == keywords) {
			keywords = defVo.getKeywords();
		}
		long value = getLongValue(keywords[0].getValue(arg));
		if (value < 0) {
			throw new ShardingException(TangYuanLang.get("sharding.value.key.invalid", keywords[0].getOriginal(), value));
		}

		int  allTable   = defVo.getDbCount() * defVo.getTableCount();

		// 如果表名递增,则直接可以获取
		long tableIndex = (value - 1) / defVo.getTableCapacity();
		if (tableIndex >= allTable) {
			throw new ShardingException(TangYuanLang.get("sharding.value.key.invalid.reason.beyond", keywords[0].getOriginal(), value));
		}
		long dbIndex = (value - 1) / (defVo.getTableCount() * defVo.getTableCapacity());
		if (dbIndex >= defVo.getDbCount()) {
			throw new ShardingException(TangYuanLang.get("sharding.value.key.invalid.reason.beyond", keywords[0].getOriginal(), value));
		}
		if (!defVo.isTableNameIndexIncrement()) {
			tableIndex = tableIndex - (dbIndex * defVo.getTableCount()); // 这是对的
		}
		return getResult(tableIndex, dbIndex, defVo, argVo);
	}

}
