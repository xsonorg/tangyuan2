package org.xson.tangyuan.sharding;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.sharding.ShardingArgVo.ShardingTemplate;
import org.xson.tangyuan.util.HashUtil;

public abstract class AbstractShardingHandler implements ShardingHandler {

	protected ShardingResult getResult(long tableIndex, long dbIndex, ShardingDefVo defVo, ShardingArgVo argVo) {
		// 还需要根据数据源类型判断是否只有一个数据库
		String dataSource = defVo.getDataSource();
		if (defVo.isDataSourceGroup()) {
			dataSource = dataSource + "." + dbIndex;
		}
		if (ShardingTemplate.DT == argVo.getTemplate()) {
			return new ShardingResult(defVo.getTable() + tableIndex, dataSource);
		} else if (ShardingTemplate.T == argVo.getTemplate()) {
			return new ShardingResult(defVo.getTable() + tableIndex, null);
		} else if (ShardingTemplate.DI == argVo.getTemplate()) {
			return new ShardingResult("" + tableIndex, dataSource);
		} else if (ShardingTemplate.I == argVo.getTemplate()) {
			return new ShardingResult("" + tableIndex, null);
		} else if (ShardingTemplate.D == argVo.getTemplate()) {
			return new ShardingResult("", dataSource);
		}
		return null;
	}

	protected ShardingResult getResult(String tableIndex, String dbIndex, ShardingDefVo defVo, ShardingArgVo argVo) {
		// 还需要根据数据源类型判断是否只有一个数据库
		String dataSource = defVo.getDataSource();
		if (defVo.isDataSourceGroup()) {
			dataSource = dataSource + "." + dbIndex;
		}
		if (ShardingTemplate.DT == argVo.getTemplate()) {
			return new ShardingResult(defVo.getTable() + tableIndex, dataSource);
		} else if (ShardingTemplate.T == argVo.getTemplate()) {
			return new ShardingResult(defVo.getTable() + tableIndex, null);
		} else if (ShardingTemplate.DI == argVo.getTemplate()) {
			return new ShardingResult(tableIndex, dataSource);
		} else if (ShardingTemplate.I == argVo.getTemplate()) {
			return new ShardingResult(tableIndex, null);
		} else if (ShardingTemplate.D == argVo.getTemplate()) {
			return new ShardingResult("", dataSource);
		}
		return null;
	}

	protected long getLongValue(Object value) {
		if (null == value) {
			//			throw new ShardingException("分库分表对象值为空");
			throw new ShardingException(TangYuanLang.get("sharding.value.empty"));
		}
		Class<?> type = value.getClass();
		if (Integer.class == type) {
			return ((Integer) value).intValue();
		} else if (Long.class == type) {
			return ((Long) value).longValue();
		} else if (BigInteger.class == type) {
			return ((BigInteger) value).longValue();
		} else if (Short.class == type) {
			return ((Short) value).shortValue();
		} else if (Byte.class == type) {
			return ((Byte) value).byteValue();
		} else if (BigDecimal.class == type) {
			return ((BigDecimal) value).longValue();
		}
		throw new ShardingException(TangYuanLang.get("sharding.value.invalid", value.toString()));
	}

	protected int getHashCode(Object value) {
		if (null == value) {
			throw new ShardingException(TangYuanLang.get("sharding.value.empty"));
		}
		return Math.abs(HashUtil.jdkHashCode(value.toString()));
	}

	protected ShardingResult selectByModulusAlgorithm(ShardingDefVo defVo, ShardingArgVo argVo, long value) {
		int  allTable   = defVo.getDbCount() * defVo.getTableCount();
		long tableIndex = value % allTable;
		long dbIndex    = tableIndex / defVo.getTableCount();
		if (!defVo.isTableNameIndexIncrement()) {
			tableIndex = tableIndex - (dbIndex * defVo.getTableCount());
		}
		return getResult(tableIndex, dbIndex, defVo, argVo);
	}

	//////////////////////////////////////////////////////////////////////////

	//		Variable[] keywords = argVo.getKeywords();
	//		if (null == keywords) {
	//			keywords = defVo.getKeywords();
	//		}
	//		Object shardingValue = keywords[0].getValue(arg);
	//		int    hashCode      = getHashCode(shardingValue);

}
