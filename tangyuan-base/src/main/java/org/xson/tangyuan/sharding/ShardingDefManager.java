package org.xson.tangyuan.sharding;

import java.util.Map;

public class ShardingDefManager {

	private Map<String, ShardingDefVo> shardingDefMap = null;

	public ShardingDefVo getShardingDef(String key) {
		return shardingDefMap.get(key);
	}

	public void setShardingDefMap(Map<String, ShardingDefVo> shardingDefMap) {
		this.shardingDefMap = shardingDefMap;
	}
}
