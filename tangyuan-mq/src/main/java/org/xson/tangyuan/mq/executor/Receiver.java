package org.xson.tangyuan.mq.executor;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.xson.common.object.XCO;
import org.xson.logging.Log;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.executor.ServiceActuator;
import org.xson.tangyuan.mq.vo.BindingPattern;
import org.xson.tangyuan.mq.vo.BindingVo;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.util.PatternMatchUtils;

public abstract class Receiver {

	AtomicInteger THREAD_INDEX = new AtomicInteger();

	protected class SyncReceiveThread extends Thread {
		public SyncReceiveThread() {
			this.setName("Receiver_Thread_" + THREAD_INDEX.getAndIncrement());
			this.setDaemon(true);
		}
	}

	abstract public void start() throws Throwable;

	abstract public void stop();

	abstract public Log getLog();

	private boolean exec(String service, XCO xcoMessage) {
		try {
			Object result = ServiceActuator.execute(service, xcoMessage);
			if (result instanceof XCO) {
				XCO xcoResult = (XCO) result;
				Integer code = xcoResult.getCode();
				if (null != code && 0 != code.intValue()) {
					return false;
				}
			}
			return true;
		} catch (Throwable e) {
			getLog().error("mq listener exec error, service: " + service + ", message: " + xcoMessage.toXMLString(), e);
		}
		return false;
	}

	protected boolean exec(String service, XCO xcoMessage, BindingVo binding) {
		if (null != binding) {
			String routingKey = getRoutingKey(binding, xcoMessage);
			if (null == routingKey) {
				throw new TangYuanException("The routingKey that the value is empty.");
			}
			List<BindingPattern> patterns = binding.getPatterns();
			boolean match = false;
			for (BindingPattern bp : patterns) {
				if (bp.isPatternMatch()) {
					match = PatternMatchUtils.simpleMatch(bp.getPattern(), routingKey);
				} else {
					match = routingKey.equals(bp.getPattern());
				}
				if (match) {
					break;
				}
			}
			if (!match) {
				getLog().info("channel [" + binding.getChannel() + "] ignores the message, because key: " + routingKey);
				return true;// 不匹配==无需处理
			}
		}
		return exec(service, xcoMessage);
	}

	private String getRoutingKey(BindingVo bVo, Object arg) {
		String routingKey = null;
		if (null == bVo) {
			return routingKey;
		}
		Object key = bVo.getKey();
		if (null == key) {
			return routingKey;
		} else if (key instanceof Variable) {
			Object tmp = ((Variable) key).getValue(arg);
			if (null == tmp) {
				throw new TangYuanException("The routingKey that the value is empty: " + ((Variable) key).getOriginal());
			}
			routingKey = tmp.toString();
		} else {
			routingKey = key.toString();
		}
		return routingKey;
	}

}
