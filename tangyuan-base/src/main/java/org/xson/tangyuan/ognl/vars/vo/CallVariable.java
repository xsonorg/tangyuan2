package org.xson.tangyuan.ognl.vars.vo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.ognl.vars.ArgSelfVo;
import org.xson.tangyuan.ognl.vars.Variable;

/**
 * 方法调用变量, 仅考虑支持JAVA静态方法
 */
public class CallVariable extends Variable {

	private Method   staticMethod;
	private Object[] vars;

	public CallVariable(String original, Method method, Object[] vars) {
		this.original = original;
		this.staticMethod = method;
		this.vars = vars;
	}

	public Object getValue(Object arg) {
		// TODO: 要区分JAVA, JS, GROVE(脚本语言)
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

	// private String method;
	// public CallVariable(String original, String method, Object[] vars) {
	// this.original = original;
	// this.method = method;
	// this.vars = vars;
	// }
	// if (null != vars && vars.length > 0) {
	// tempArgs = new Object[vars.length];
	// for (int i = 0; i < vars.length; i++) {
	// if (vars[i] instanceof VariableItemWraper) {
	// tempArgs[i] = ((VariableItemWraper) vars[i]).getValue(arg);
	// } else if (vars[i] == ArgSelfVo.argSelf) {
	// tempArgs[i] = arg;
	// } else {
	// tempArgs[i] = vars[i];
	// }
	// }
	// }
	// if (null == tempArgs) {
	// return staticMethod.invoke(null);
	// } else {
	// return staticMethod.invoke(null, tempArgs);
	// }
}
