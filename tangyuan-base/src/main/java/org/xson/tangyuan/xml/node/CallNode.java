package org.xson.tangyuan.xml.node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.executor.ServiceActuator;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.ognl.Ognl;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.util.TangYuanUtil;
import org.xson.tangyuan.xml.node.vo.PropertyItem;

/**
 * 调用节点
 * 
 * <pre>
 * 当前执行模式:通过mode属性控制 <br />
 * 目标执行模式:通过URL控制<br />
 */
public class CallNode implements TangYuanNode {

	private Object				service;
	private String				resultKey;
	/** 本地当前执行的模式 */
	private CallMode			mode;
	private List<PropertyItem>	itemList;
	/** 当发生异常时候, 异常结果的描述key */
	// private String exResultKey;

	// 错误编码和错误信息
	private String				codeKey;
	private String				messageKey;

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

	// public CallNode(Object service, String resultKey, CallMode mode, List<PropertyItem> itemList, String exResultKey) {
	// this.service = service;
	// this.resultKey = resultKey;
	// this.mode = mode;
	// if (null == this.mode) {
	// this.mode = CallMode.EXTEND;
	// }
	// this.itemList = itemList;
	// // this.exResultKey = exResultKey;
	// if (!(this.service instanceof String)) {
	// dynamicCall = true;
	// }
	// }

	public CallNode(Object service, String resultKey, CallMode mode, List<PropertyItem> itemList, String codeKey, String messageKey) {
		this.service = service;
		this.resultKey = resultKey;
		this.mode = mode;
		if (null == this.mode) {
			this.mode = CallMode.EXTEND;
		}
		this.itemList = itemList;
		// this.exResultKey = exResultKey;

		this.codeKey = codeKey;
		this.messageKey = messageKey;

		if (!(this.service instanceof String)) {
			dynamicCall = true;
		}
	}

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
				// Ognl.setValue(arg, this.resultKey, result);
				Ognl.setValue(arg, this.resultKey, TangYuanUtil.getRealData(result));
			}
			// 设置异常CODE/MESSAGE
			setResultInfo(arg, result);

			// 这里的异常上抛
		} else if (CallMode.ALONE == _mode) {

			Object result = ServiceActuator.executeAlone(_service, parameter);
			if (null != this.resultKey) {
				Ognl.setValue(arg, this.resultKey, TangYuanUtil.getRealData(result));
			}

			setResultInfo(arg, result);

		} else {
			ServiceActuator.executeAsync(_service, parameter);
		}
		return true;
	}

	@SuppressWarnings("rawtypes")
	private void setResultInfo(Object arg, Object result) {
		if (null == result) {
			return;
		}
		if (result instanceof XCO) {
			XCO xco = (XCO) result;
			if (null != this.codeKey && xco.exists(TangYuanContainer.XCO_CODE_KEY)) {
				Ognl.setValue(arg, this.codeKey, xco.getCode());
			}
			if (null != this.messageKey && xco.exists(TangYuanContainer.XCO_MESSAGE_KEY)) {
				Ognl.setValue(arg, this.messageKey, xco.getMessage());
			}
		}

		if (result instanceof Map) {
			Map map = (Map) result;
			if (null != this.codeKey && map.containsKey(TangYuanContainer.XCO_CODE_KEY)) {
				Ognl.setValue(arg, this.codeKey, map.get(TangYuanContainer.XCO_CODE_KEY));
			}
			if (null != this.messageKey && map.containsKey(TangYuanContainer.XCO_MESSAGE_KEY)) {
				Ognl.setValue(arg, this.messageKey, map.get(TangYuanContainer.XCO_MESSAGE_KEY));
			}
		}
	}

	// try {
	// Object result = ServiceActuator.executeAlone(_service, parameter);
	// if (null != this.resultKey) {
	// Ognl.setValue(arg, this.resultKey, result);
	// }
	// } catch (ServiceException e) {
	// if (null != exResultKey) {
	// Object result = getResultWithException(_service, arg, e);
	// Ognl.setValue(arg, this.exResultKey, result);
	// }
	// log.error("call service error: " + service, e);
	// }

	// /** 放置错误信息 */
	// private Object getResultWithException(String _service, Object arg, Throwable ex) {
	// int errorCode = -1;
	// String errorMessage = "Bridge call exception";// service
	// if (ex instanceof ServiceException) {
	// ServiceException sex = (ServiceException) ex;
	// errorCode = sex.getErrorCode();
	// errorMessage = sex.getErrorMessage();
	// }

	// if (XCO.class == arg.getClass()) {
	// XCO xco = new XCO();
	// xco.setIntegerValue("code", errorCode);
	// xco.setStringValue("message", errorMessage);
	// return xco;
	// } else if (Map.class.isAssignableFrom(arg.getClass())) {
	// Map<String, Object> map = new HashMap<String, Object>();
	// map.put("code", errorCode);
	// map.put("message", errorMessage);
	// return map;
	// } else {
	// throw new TangYuanException("Unsupported parameter type: " + arg.getClass());
	// }
	// }

}
