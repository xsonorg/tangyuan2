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

}
