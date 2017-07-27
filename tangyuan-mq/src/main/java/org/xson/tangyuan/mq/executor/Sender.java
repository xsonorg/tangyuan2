package org.xson.tangyuan.mq.executor;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.mq.vo.ChannelVo;
import org.xson.tangyuan.mq.vo.RoutingVo;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.util.PatternMatchUtils;

public abstract class Sender {

	abstract public void sendMessage(ChannelVo queue, RoutingVo rVo, Object arg, boolean useTx, MqServiceContext context) throws Throwable;

	protected String getRoutingKey(RoutingVo rVo, Object arg) {
		String routingKey = null;
		if (null == rVo) {
			return routingKey;
		}
		Object key = rVo.getKey();
		if (null == key) {
			return routingKey;
		} else if (key instanceof Variable) {
			Object tmp = ((Variable) key).getValue(arg);
			if (null == tmp) {
				throw new TangYuanException("The routingKey that the value is empty, key: " + ((Variable) key).getOriginal());
			}
			routingKey = tmp.toString();
		} else {
			routingKey = key.toString();
		}
		return routingKey;
	}

	/**
	 * 是否路由
	 */
	protected boolean isRouting(RoutingVo rVo, Object arg) {
		if (null == rVo) {
			return true;
		}
		String routingKey = getRoutingKey(rVo, arg);
		if (null == routingKey) {
			throw new TangYuanException("The routingKey that the value is empty.");
		}
		if (rVo.isPatternMatch()) {
			return PatternMatchUtils.simpleMatch(rVo.getPattern(), routingKey);
		} else {
			return routingKey.equals(rVo.getPattern());
		}
	}

}
