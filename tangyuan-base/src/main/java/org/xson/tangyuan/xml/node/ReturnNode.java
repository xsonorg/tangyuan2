package org.xson.tangyuan.xml.node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.common.object.XCO;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.xml.node.vo.PropertyItem;

public class ReturnNode implements TangYuanNode {

	private Class<?>			resultType;

	/**
	 * 直接返回一个对象
	 */
	private Object				resultValue;

	/**
	 * 返回多个对象
	 */
	private List<PropertyItem>	resultList;

	// private List<ReturnItem> resultList;
	// public static class ReturnItem {
	// public ReturnItem(String name, Object value) {
	// this.name = name;
	// this.value = value;
	// }
	// String name;
	// Object value;
	// }
	// public ReturnNode(Object resultValue, List<ReturnItem> resultList, Class<?> resultType) {
	// this.resultValue = resultValue;
	// this.resultList = resultList;
	// this.resultType = resultType;
	// }

	public ReturnNode(Object resultValue, List<PropertyItem> resultList, Class<?> resultType) {
		this.resultValue = resultValue;
		this.resultList = resultList;
		this.resultType = resultType;
	}

	@Override
	public boolean execute(ServiceContext context, Object arg) {
		if (null == resultValue) {
			if (Map.class == resultType) {
				Map<String, Object> map = new HashMap<String, Object>();
				for (PropertyItem item : resultList) {
					if (item.value instanceof Variable) {
						map.put(item.name, ((Variable) item.value).getValue(arg));
					} else {
						map.put(item.name, item.value);
					}
				}
				context.setResult(map);
			} else if (XCO.class == resultType) {
				XCO xco = new XCO();
				for (PropertyItem item : resultList) {
					if (item.value instanceof Variable) {
						xco.setObjectValue(item.name, ((Variable) item.value).getValue(arg));
					} else {
						xco.setObjectValue(item.name, item.value);
					}
				}
				context.setResult(xco);
			}
		} else {
			if (resultValue instanceof Variable) {
				context.setResult(((Variable) resultValue).getValue(arg));
			} else {
				context.setResult(resultValue);
			}
		}
		return true;
	}

	// @Override
	// public boolean execute(ServiceContext context, Object arg) {
	// if (Map.class == resultType) {
	// if (null == resultValue) {
	// Map<String, Object> map = new HashMap<String, Object>();
	// for (PropertyItem item : resultList) {
	// if (item.value instanceof Variable) {
	// map.put(item.name, ((Variable) item.value).getValue(arg));
	// } else {
	// map.put(item.name, item.value);
	// }
	// }
	// context.setResult(map);
	// } else {
	// if (resultValue instanceof Variable) {
	// context.setResult(((Variable) resultValue).getValue(arg));
	// } else {
	// context.setResult(resultValue);
	// }
	// }
	// } else if (XCO.class == resultType) {
	// if (null == resultValue) {
	// XCO xco = new XCO();
	// for (PropertyItem item : resultList) {
	// if (item.value instanceof Variable) {
	// xco.setObjectValue(item.name, ((Variable) item.value).getValue(arg));
	// } else {
	// xco.setObjectValue(item.name, item.value);
	// }
	// }
	// context.setResult(xco);
	// } else {
	// if (resultValue instanceof Variable) {
	// context.setResult(((Variable) resultValue).getValue(arg));
	// } else {
	// context.setResult(resultValue);
	// }
	// }
	// }
	// return true;
	// }
}
