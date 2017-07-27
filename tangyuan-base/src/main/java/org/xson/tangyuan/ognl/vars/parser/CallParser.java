package org.xson.tangyuan.ognl.vars.parser;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.xson.tangyuan.ognl.OgnlException;
import org.xson.tangyuan.ognl.vars.ArgSelfVo;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.ognl.vars.vo.CallVariable;
import org.xson.tangyuan.util.ClassUtils;

public class CallParser extends AbstractParser {

	public boolean check(String text) {
		if (text.startsWith("@")) {
			return true;
		}
		return false;
	}

	private Object getVal(String text) {
		if ("null".equalsIgnoreCase(text)) {
			return null;
		}
		if ("true".equalsIgnoreCase(text) || "false".equalsIgnoreCase(text)) {
			return Boolean.valueOf(text);
		}
		// 仅仅支持:数值类型, 字符串类型
		// if (isStaticString(text)) { // 字符串
		// return text.substring(1, text.length() - 1);
		// } else if (isNumber(text)) { // 数值
		// return getNumber(text);
		// } else {// 变量
		// return new NormalParser().parse(text);
		// }

		if (isStaticString(text)) { // 字符串
			return text.substring(1, text.length() - 1);
		} else if (isNumber(text)) { // 数值
			return getNumber(text);
		} else if (ArgSelfVo.AEG_SELF_MARK.equalsIgnoreCase(text)) { // 特殊参数
			return ArgSelfVo.argSelf;
		} else {// 变量
			return new NormalParser().parse(text);
		}
	}

	/**
	 * 解析调用表达式属性
	 */
	public Variable parse(String text) {
		text = text.trim();
		int left = text.indexOf("(");
		int right = text.lastIndexOf(")");

		if (left == -1 || right == -1 || left > right) {
			throw new OgnlException("不合法的调用表达式: " + text);
		}

		// TODO: 要区分Java, JS
		// 仅考虑支持JAVA静态方法
		String methodName = text.substring(1, left).trim();
		String argString = text.substring(left + 1, right).trim();

		Object[] vars = null;
		if (argString.length() > 0) {
			String[] array = argString.split(",");
			vars = new Object[array.length];
			for (int i = 0; i < array.length; i++) {
				vars[i] = getVal(array[i].trim());
			}
		}

		Method method = getStaticMethod(methodName);
		return new CallVariable(text, method, vars);
	}

	private Method getStaticMethod(String fullName) {
		int lastpos = fullName.lastIndexOf(".");
		if (lastpos < 0) {
			throw new OgnlException("Illegal method call name: " + fullName);
		}

		String className = fullName.substring(0, lastpos);
		String methodName = fullName.substring(lastpos + 1);
		Class<?> clazz = ClassUtils.forName(className);

		Method[] methods = clazz.getMethods();
		for (Method m : methods) {
			if (m.getName().equals(methodName)) {
				if (!Modifier.isStatic(m.getModifiers())) {
					throw new OgnlException("The method invoked in XML must be static: " + fullName);
				}
				return m;
			}
		}

		throw new OgnlException("Non-existent method call name: " + fullName);
	}

	public static void main(String[] args) {
		CallParser p = new CallParser();
		// String text = "@xxx(ccc, 's', 0L) ";
		String text = "@xxx() ";
		System.out.println(p.parse(text));
		// System.out.println("****************************");
	}

}
