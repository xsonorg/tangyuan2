package org.xson.tangyuan.ognl.vars.vo;

import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.sharding.ShardingArgVo;

/**
 * 分片变量
 */
public class ShardingVariable extends Variable {

	private ShardingArgVo shardingArg;

	public ShardingVariable(String original, ShardingArgVo shardingArg) {
		this.original = original;
		this.shardingArg = shardingArg;
	}

	@Override
	public Object getValue(Object arg) {
		return this.shardingArg.getShardingResult(arg);
	}
}
