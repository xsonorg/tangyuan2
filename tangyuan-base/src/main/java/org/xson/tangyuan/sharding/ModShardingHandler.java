package org.xson.tangyuan.sharding;

import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.ognl.vars.Variable;

public class ModShardingHandler extends AbstractShardingHandler {

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

		return selectByModulusAlgorithm(defVo, argVo, value);
	}

	//	@Override
	//	public ShardingResult selectDataSourceAndTable(ShardingDefVo defVo, ShardingArgVo argVo, Object arg) {
	//		Variable[] keywords = argVo.getKeywords();
	//		if (null == keywords) {
	//			keywords = defVo.getKeywords();
	//		}
	//		long value = getValue(keywords[0].getValue(arg));
	//		if (value < 0) {
	//			throw new ShardingException("分库分表对象值非法: " + value);
	//		}
	//		// // <!--按照mod划分, 1.key mod dbCount(dbCount > 1), 2, key mode talbeCount(tableCount > 1)-->
	//		// long tableIndex = value % defVo.getTableCount();
	//		// long dbIndex = value % defVo.getDbCount();
	//		// if (defVo.isTableNameIndexIncrement()) {
	//		// tableIndex = tableIndex + (dbIndex * defVo.getTableCount());
	//		// }
	//
	//		// fix bug with TableNameIndexIncrement
	//		// long dbIndex = -1;
	//		// long tableIndex = -1;
	//		// if (defVo.isTableNameIndexIncrement()) {
	//		// int allTable = defVo.getDbCount() * defVo.getTableCount();
	//		// tableIndex = value % allTable;
	//		// dbIndex = tableIndex / defVo.getTableCount();
	//		// } else {
	//		// // <!--按照mod划分, 1.key mod dbCount(dbCount > 1), 2, key mode talbeCount(tableCount > 1)-->
	//		// dbIndex = value % defVo.getDbCount();
	//		// tableIndex = value % defVo.getTableCount();
	//		// }
	//		// if (-1 == dbIndex || -1 == tableIndex) {
	//		// throw new ShardingException("分库分表索引不合法");
	//		// }
	//
	//		int allTable = defVo.getDbCount() * defVo.getTableCount();
	//		long tableIndex = value % allTable;
	//		long dbIndex = tableIndex / defVo.getTableCount();
	//		if (!defVo.isTableNameIndexIncrement()) {
	//			tableIndex = tableIndex - (dbIndex * defVo.getTableCount());
	//		}
	//
	//		return getResult(tableIndex, dbIndex, defVo, argVo);
	//	}

}
