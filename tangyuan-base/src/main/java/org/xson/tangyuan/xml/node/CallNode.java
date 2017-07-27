package org.xson.tangyuan.xml.node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.common.object.XCO;
import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.executor.ServiceActuator;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.executor.ServiceException;
import org.xson.tangyuan.ognl.Ognl;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.xml.node.vo.PropertyItem;

/**
 * 调用节点
 * 
 * <pre>
 * 当前执行模式:通过mode属性控制 <br />
 * 目标执行模式:通过URL控制<br />
 */
public class CallNode implements TangYuanNode {

	private static Log			log			= LogFactory.getLog(CallNode.class);

	private Object				service;
	private String				resultKey;
	/** 本地当前执行的模式 */
	private CallMode			mode;
	private List<PropertyItem>	itemList;
	/** 当发生异常时候, 异常结果的描述key */
	private String				exResultKey;

	/** 桥接调用[0:不确认, 1:桥接, 2:本地] */
	// private BridgedCallMode bridgedCall = BridgedCallMode.DYNAMIC;

	/** 动态调用 */
	private boolean				dynamicCall	= false;

	public enum CallMode {
		/** 继承之前的上下文 */
		EXTEND,
		/** 单独的上下文 */
		ALONE,
		/** 异步方式 */
		ASYNC
	}

	// enum BridgedCallMode {
	// /** 动态的, 需要每次计算 */
	// DYNAMIC,
	// /** 桥接 */
	// BRIDGED,
	// /** 本地 */
	// LOCAL
	// }

	// public CallNode(Object service, String resultKey, CallMode mode, List<PropertyItem> itemList, String exResultKey) {
	// this.service = service;
	// this.resultKey = resultKey;
	// this.mode = mode;
	// if (null == this.mode) {
	// this.mode = CallMode.EXTEND;
	// }
	// this.itemList = itemList;
	// this.exResultKey = exResultKey;
	// if (this.service instanceof String) {
	// String _service = (String) this.service;
	// this.bridgedCall = getBridged(_service);
	// }
	// }

	public CallNode(Object service, String resultKey, CallMode mode, List<PropertyItem> itemList, String exResultKey) {
		this.service = service;
		this.resultKey = resultKey;
		this.mode = mode;
		if (null == this.mode) {
			this.mode = CallMode.EXTEND;
		}
		this.itemList = itemList;
		this.exResultKey = exResultKey;
		if (!(this.service instanceof String)) {
			dynamicCall = true;
		}
	}

	// private BridgedCallMode getBridged(String _service) {
	// if (BridgedCallSupport.isBridged(_service)) {
	// return BridgedCallMode.BRIDGED;
	// }
	// return BridgedCallMode.LOCAL;
	// }

	@Override
	public boolean execute(ServiceContext context, Object arg) {
		Object parameter = arg;
		if (null != itemList) {
			// 基于实际的参数来转换
			if (XCO.class == arg.getClass()) {
				XCO xco = new XCO();
				for (PropertyItem item : itemList) {
					// xco.setObjectValue(item.name, item.value.getValue(arg));
					if (item.value instanceof Variable) {
						xco.setObjectValue(item.name, ((Variable) item.value).getValue(arg));
					} else {
						xco.setObjectValue(item.name, item.value);
					}
				}
				parameter = xco;
			} else if (Map.class.isAssignableFrom(arg.getClass())) {
				Map<String, Object> map = new HashMap<String, Object>();
				for (PropertyItem item : itemList) {
					// map.put(item.name, item.value.getValue(arg));
					if (item.value instanceof Variable) {
						map.put(item.name, ((Variable) item.value).getValue(arg));
					} else {
						map.put(item.name, item.value);
					}
				}
				parameter = map;
			} else {
				throw new TangYuanException("Unsupported parameter type: " + arg.getClass());
			}
		}

		String _service = null;
		// BridgedCallMode _bridgedCall = this.bridgedCall;
		// if (BridgedCallMode.DYNAMIC != _bridgedCall) {
		// _service = (String) this.service;
		// } else {
		// _service = (String) ((NormalVariable) this.service).getValue(arg);
		// if (null == _service) {
		// throw new TangYuanException("The calling service name variable is null: " + ((NormalVariable) this.service).getOriginal());
		// }
		// _bridgedCall = getBridged(_service);
		// }

		if (dynamicCall) {
			_service = (String) ((Variable) this.service).getValue(arg);
			if (null == _service) {
				throw new TangYuanException("The calling service name variable is null: " + ((Variable) this.service).getOriginal());
			}
		} else {
			_service = (String) this.service;
		}

		CallMode _mode = this.mode;

		if (CallMode.EXTEND == _mode) {
			Object result = ServiceActuator.execute(_service, parameter);
			if (null != this.resultKey) {
				Ognl.setValue(arg, this.resultKey, result);
			}
			// 这里的异常上抛
		} else if (CallMode.ALONE == _mode) {
			try {
				Object result = ServiceActuator.executeAlone(_service, parameter);
				if (null != this.resultKey) {
					Ognl.setValue(arg, this.resultKey, result);
				}
			} catch (ServiceException e) {
				if (null != exResultKey) {
					Object result = getResultWithException(_service, arg, e);
					Ognl.setValue(arg, this.exResultKey, result);
				}
				log.error("call service error: " + service, e);
			}
		} else {
			ServiceActuator.executeAsync(_service, parameter);
		}
		return true;
	}

	/** 放置错误信息 */
	private Object getResultWithException(String _service, Object arg, Throwable ex) {
		int errorCode = -1;
		String errorMessage = "Bridge call exception";// service
		if (ex instanceof ServiceException) {
			ServiceException sex = (ServiceException) ex;
			errorCode = sex.getErrorCode();
			errorMessage = sex.getErrorMessage();
		}

		if (XCO.class == arg.getClass()) {
			XCO xco = new XCO();
			xco.setIntegerValue("code", errorCode);
			xco.setStringValue("message", errorMessage);
			return xco;
		} else if (Map.class.isAssignableFrom(arg.getClass())) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("code", errorCode);
			map.put("message", errorMessage);
			return map;
		} else {
			throw new TangYuanException("Unsupported parameter type: " + arg.getClass());
		}
	}

}
