package org.xson.tangyuan.xml.node;

import java.util.List;

import org.xson.common.object.XCO;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.service.ActuatorContext;
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

	public ReturnNode(Object resultValue, List<PropertyItem> resultList, Class<?> resultType) {
		this.resultValue = resultValue;
		this.resultList = resultList;
		this.resultType = resultType;
	}

	public boolean execute(ActuatorContext ac, Object arg, Object temp) throws Throwable {
		if (null == resultValue) {
			if (XCO.class == resultType) {
				XCO xco = new XCO();
				for (PropertyItem item : resultList) {
					if (item.value instanceof Variable) {
						xco.setObjectValue(item.name, ((Variable) item.value).getValue(temp));
					} else {
						xco.setObjectValue(item.name, item.value);
					}
				}
				ac.setResult(xco);
			}
		} else {
			if (resultValue instanceof Variable) {
				ac.setResult(((Variable) resultValue).getValue(temp));
			} else {
				ac.setResult(resultValue);
			}
		}
		return true;
	}

}
