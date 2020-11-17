package org.xson.tangyuan.xml.node;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.ognl.Ognl;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.service.ActuatorContext;

public abstract class ForNode implements TangYuanNode {

	protected TangYuanNode sqlNode   = null;
	/** 变量名称 */
	protected String       index     = null;
	/** 开始索引, 可以是常量或者变量 */
	protected Object       start     = null;
	/** 结束索引, 不包括 */
	protected Object       end       = null;

	/** 可选的 */
	protected String       open      = null;
	protected String       close     = null;
	protected String       separator = null;

	@Override
	public boolean execute(ActuatorContext ac, Object arg, Object acArg) throws Throwable {

		int startVal = getIntValue(start, acArg, "start");
		int endVal   = getIntValue(end, acArg, "end");
		if (endVal <= startVal || endVal < 1) {
			throw new TangYuanException("无效的属性[start:" + startVal + "], [end:" + endVal + "]");
		}

		append(ac, open);
		for (int i = startVal; i < endVal; i++) {
			Ognl.setValue(acArg, index, i);
			if (i > startVal) {
				append(ac, separator);
			}
			sqlNode.execute(ac, arg, acArg);
		}
		append(ac, close);
		return true;
	}

	private int getIntValue(Object val, Object acArg, String msg) {
		try {
			if (val instanceof Variable) {
				val = ((Variable) val).getValue(acArg);
			}
			return ((Integer) val).intValue();
		} catch (Throwable e) {
			throw new TangYuanException("无效的属性'" + msg + "': " + val.toString());
		}
	}

	protected abstract void append(ActuatorContext ac, String str);

}
