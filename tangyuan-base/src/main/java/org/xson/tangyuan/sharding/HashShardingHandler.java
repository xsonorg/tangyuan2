package org.xson.tangyuan.sharding;

import org.xson.tangyuan.ognl.vars.Variable;

public class HashShardingHandler extends AbstractShardingHandler {

	@Override
	public ShardingResult selectDataSourceAndTable(ShardingDefVo defVo, ShardingArgVo argVo, Object arg) {
		Variable[] keywords = argVo.getKeywords();
		if (null == keywords) {
			keywords = defVo.getKeywords();
		}
		Object shardingValue = keywords[0].getValue(arg);
		int hashCode = getHashCode(shardingValue);

		return selectByModulusAlgorithm(defVo, argVo, hashCode);
	}

}
