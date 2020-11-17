package org.xson.tangyuan.xml.node;

import java.util.List;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.ognl.Ognl;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.service.Actuator;
import org.xson.tangyuan.service.ActuatorContext;
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

	private Object             service     = null;
	private String             resultKey   = null;
	/** 本地当前执行的模式 */
	private CallMode           mode        = null;
	private List<PropertyItem> itemList    = null;
	/**
	 * 调用后的结果编码
	 */
	private String             codeKey     = null;
	/**
	 * 调用后的错误信息
	 */
	private String             messageKey  = null;
	/** 动态调用 */
	private boolean            dynamicCall = false;
	/**
	 * 是否追加参数，从原始的arg中
	 */
	private boolean            appendArg   = false;

	public enum CallMode {
		/** 同步方式 */
		SYNC,
		/** 异步方式 */
		ASYNC;
	}

	public CallNode(Object service, String resultKey, CallMode mode, List<PropertyItem> itemList, String codeKey, String messageKey) {
		this.service = service;
		this.resultKey = resultKey;
		this.mode = mode;
		this.itemList = itemList;
		this.codeKey = codeKey;
		this.messageKey = messageKey;
		if (!(this.service instanceof String)) {
			dynamicCall = true;
		}
	}

	@Override
	public boolean execute(ActuatorContext ac, Object arg, Object acArg) throws Throwable {
		Object parameter = arg;
		if (null != itemList) {
			XCO xco = null;
			if (appendArg) {
				// 支持传入原始的arg参数
				xco = ((XCO) arg).clone();
			} else {
				xco = new XCO();
			}
			for (PropertyItem item : itemList) {
				if (item.value instanceof Variable) {
					xco.setObjectValue(item.name, ((Variable) item.value).getValue(acArg));
				} else {
					xco.setObjectValue(item.name, item.value);
				}
			}
			parameter = xco;
		}

		String serviceURI = null;

		if (dynamicCall) {
			serviceURI = (String) ((Variable) this.service).getValue(acArg);
			if (null == serviceURI) {
				throw new TangYuanException("The calling service name variable is null: " + ((Variable) this.service).getOriginal());
			}
		} else {
			serviceURI = (String) this.service;
		}

		CallMode _mode = this.mode;

		if (CallMode.ASYNC == _mode) {
			Actuator.executeAsync(serviceURI, parameter);
			if (null != this.codeKey) {
				Ognl.setValue(acArg, this.codeKey, TangYuanContainer.SUCCESS_CODE);
			}
		} else {
			XCO result = Actuator.execute(serviceURI, parameter);
			if (null != this.resultKey) {
				Ognl.setValue(acArg, this.resultKey, TangYuanUtil.getRealData(result));
			}
			// 设置异常CODE/MESSAGE
			setErrorInfo(acArg, result);
		}
		return true;
	}

	private void setErrorInfo(Object acArg, Object result) {
		if (null == result) {
			return;
		}
		if (result instanceof XCO) {
			XCO xco = (XCO) result;
			if (null != this.codeKey && xco.exists(TangYuanContainer.XCO_CODE_KEY)) {
				Ognl.setValue(acArg, this.codeKey, xco.getCode());
			}
			if (null != this.messageKey && xco.exists(TangYuanContainer.XCO_MESSAGE_KEY)) {
				Ognl.setValue(acArg, this.messageKey, xco.getMessage());
			}
		}
	}

	////////////////////////////////////////////////////////////////////////

	//	public enum CallMode {
	//		/** 继承之前的上下文 */
	//		EXTEND,
	//		/** 单独的上下文 */
	//		ALONE,
	//		/** 异步方式 */
	//		ASYNC
	//	}

	//	@Override
	//	public boolean execute(ActuatorContext ac, Object arg, Object temp) throws Throwable {
	//		Object parameter = arg;
	//		if (null != itemList) {
	//			XCO xco = new XCO();
	//			for (PropertyItem item : itemList) {
	//				if (item.value instanceof Variable) {
	//					xco.setObjectValue(item.name, ((Variable) item.value).getValue(temp));
	//				} else {
	//					xco.setObjectValue(item.name, item.value);
	//				}
	//			}
	//			parameter = xco;
	//		}
	//
	//		String serviceURI = null;
	//
	//		if (dynamicCall) {
	//			serviceURI = (String) ((Variable) this.service).getValue(temp);
	//			if (null == serviceURI) {
	//				throw new TangYuanException("The calling service name variable is null: " + ((Variable) this.service).getOriginal());
	//			}
	//		} else {
	//			serviceURI = (String) this.service;
	//		}
	//
	//		CallMode _mode = this.mode;
	//
	//		if (CallMode.EXTEND == _mode) {
	//			Object result = ServiceActuator.execute(serviceURI, parameter);
	//			if (null != this.resultKey) {
	//				Ognl.setValue(temp, this.resultKey, TangYuanUtil.getRealData(result));
	//			}
	//			// 这里的异常上抛，所以不需要设置异常信息 
	//			//			if (null != this.codeKey) {
	//			//				setErrorInfo(temp, result);
	//			//			}
	//		} else if (CallMode.ALONE == _mode) {
	//			Object result = ServiceActuator.executeAlone(serviceURI, parameter);
	//			if (null != this.resultKey) {
	//				Ognl.setValue(temp, this.resultKey, TangYuanUtil.getRealData(result));
	//			}
	//			// 设置异常CODE/MESSAGE
	//			setErrorInfo(temp, result);
	//		} else {
	//			ServiceActuator.executeAsync(serviceURI, parameter);
	//		}
	//		return true;
	//	}
	//
	//	private void setErrorInfo(Object temp, Object result) {
	//		if (null == result) {
	//			return;
	//		}
	//		if (result instanceof XCO) {
	//			XCO xco = (XCO) result;
	//			if (null != this.codeKey && xco.exists(TangYuanContainer.XCO_CODE_KEY)) {
	//				Ognl.setValue(temp, this.codeKey, xco.getCode());
	//			}
	//			if (null != this.messageKey && xco.exists(TangYuanContainer.XCO_MESSAGE_KEY)) {
	//				Ognl.setValue(temp, this.messageKey, xco.getMessage());
	//			}
	//		}
	//	}

	// 

	//	@Override
	//	public boolean execute(ServiceContext context, Object arg) {
	//		Object parameter = arg;
	//		if (null != itemList) {
	//			// 基于实际的参数来转换
	//			if (XCO.class == arg.getClass()) {
	//				XCO xco = new XCO();
	//				for (PropertyItem item : itemList) {
	//					// xco.setObjectValue(item.name, item.value.getValue(arg));
	//					if (item.value instanceof Variable) {
	//						xco.setObjectValue(item.name, ((Variable) item.value).getValue(arg));
	//					} else {
	//						xco.setObjectValue(item.name, item.value);
	//					}
	//				}
	//				parameter = xco;
	//			} else if (Map.class.isAssignableFrom(arg.getClass())) {
	//				Map<String, Object> map = new HashMap<String, Object>();
	//				for (PropertyItem item : itemList) {
	//					// map.put(item.name, item.value.getValue(arg));
	//					if (item.value instanceof Variable) {
	//						map.put(item.name, ((Variable) item.value).getValue(arg));
	//					} else {
	//						map.put(item.name, item.value);
	//					}
	//				}
	//				parameter = map;
	//			} else {
	//				throw new TangYuanException("Unsupported parameter type: " + arg.getClass());
	//			}
	//		}
	//
	//		String _service = null;
	//
	//		if (dynamicCall) {
	//			_service = (String) ((Variable) this.service).getValue(arg);
	//			if (null == _service) {
	//				throw new TangYuanException("The calling service name variable is null: " + ((Variable) this.service).getOriginal());
	//			}
	//		} else {
	//			_service = (String) this.service;
	//		}
	//
	//		CallMode _mode = this.mode;
	//
	//		if (CallMode.EXTEND == _mode) {
	//			Object result = ServiceActuator.execute(_service, parameter);
	//			if (null != this.resultKey) {
	//				// Ognl.setValue(arg, this.resultKey, result);
	//				Ognl.setValue(arg, this.resultKey, TangYuanUtil.getRealData(result));
	//			}
	//			// 设置异常CODE/MESSAGE
	//			setResultInfo(arg, result);
	//
	//			// 这里的异常上抛
	//		} else if (CallMode.ALONE == _mode) {
	//
	//			Object result = ServiceActuator.executeAlone(_service, parameter);
	//			if (null != this.resultKey) {
	//				Ognl.setValue(arg, this.resultKey, TangYuanUtil.getRealData(result));
	//			}
	//
	//			setResultInfo(arg, result);
	//
	//		} else {
	//			ServiceActuator.executeAsync(_service, parameter);
	//		}
	//		return true;
	//	}

	//		if (result instanceof Map) {
	//			Map map = (Map) result;
	//			if (null != this.codeKey && map.containsKey(TangYuanContainer.XCO_CODE_KEY)) {
	//				Ognl.setValue(arg, this.codeKey, map.get(TangYuanContainer.XCO_CODE_KEY));
	//			}
	//			if (null != this.messageKey && map.containsKey(TangYuanContainer.XCO_MESSAGE_KEY)) {
	//				Ognl.setValue(arg, this.messageKey, map.get(TangYuanContainer.XCO_MESSAGE_KEY));
	//			}
	//		}
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

	/** 当发生异常时候, 异常结果的描述key */
	// private String exResultKey;
	//		if (null == this.mode) {
	//			//			this.mode = CallMode.EXTEND;
	//			this.mode = CallMode.ALONE;
	//		}
}
