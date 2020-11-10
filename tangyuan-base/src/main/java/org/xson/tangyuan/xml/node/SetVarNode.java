package org.xson.tangyuan.xml.node;

import org.xson.tangyuan.ognl.Ognl;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.service.ActuatorContext;

public class SetVarNode implements TangYuanNode {

	// 只需要支持最简单的key
	private String  key;

	private Object  value;

	// type="Integer"只有在变量的时候才有意义
	private boolean constant;

	public SetVarNode(String key, Object value, boolean constant) {
		this.key = key;
		this.value = value;
		this.constant = constant;
	}

	@Override
	public boolean execute(ActuatorContext ac, Object arg, Object temp) {
		if (constant) {
			Ognl.setValue(temp, key, value);
		} else {
			Ognl.setValue(temp, key, ((Variable) value).getValue(temp));
		}
		return true;
	}

	//////////////////////////////////////////////////////////////////////////////

	//	@Override
	//	public boolean execute(ServiceContext context, Object arg) {
	//		if (constant) {
	//			Ognl.setValue(arg, key, value);
	//		} else {
	//			Ognl.setValue(arg, key, ((Variable) value).getValue(arg));
	//		}
	//		return true;
	//	}
}
