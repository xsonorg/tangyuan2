package org.xson.tangyuan.ognl.vars.vo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.ognl.vars.ArgSelfVo;
import org.xson.tangyuan.ognl.vars.Variable;

/**
 * 方法调用变量, 仅考虑支持JAVA静态方法: 要区分JAVA, JS, GROVE(脚本语言)
 */
public class CallVariable extends Variable {

	private Method		staticMethod;
	private Object[]	vars;

	public CallVariable(String original, Method method, Object[] vars) {
		this.original = original;
		this.staticMethod = method;
		this.vars = vars;
	}

	public Object getValue(Object arg) {
		try {
			Object[] tempArgs = null;

			if (null != vars && vars.length > 0) {
				tempArgs = new Object[vars.length];
				for (int i = 0; i < vars.length; i++) {
					if (vars[i] instanceof Variable) {
						tempArgs[i] = ((Variable) vars[i]).getValue(arg);
					} else if (vars[i] == ArgSelfVo.argSelf) {
						tempArgs[i] = arg;
					} else {
						tempArgs[i] = vars[i];
					}
				}
			}

			return staticMethod.invoke(null, tempArgs);

		} catch (Throwable e) {
			if (e instanceof InvocationTargetException) {
				throw new TangYuanException(((InvocationTargetException) e).getTargetException());
			} else {
				throw new TangYuanException(e);
			}
		}
	}

}
