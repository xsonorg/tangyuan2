package org.xson.tangyuan.sharding;

public interface ShardingHandler {

	public ShardingResult selectDataSourceAndTable(ShardingDefVo defVo, ShardingArgVo argVo, Object arg);

}
