package org.xson.tangyuan.ognl.vars.parser;

import java.lang.reflect.Method;
import java.util.List;

import org.xson.tangyuan.ognl.OgnlException;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.ognl.vars.vo.CallVariable;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.util.XmlTextParseUtil;
import org.xson.tangyuan.xml.method.XmlStaticMethodContainer;

/**
 * 文本表达式，方法调用解析
 * 
 * 后面要区分Java, JS, 仅考虑支持JAVA静态方法
 */
public class CallParser extends AbstractParser {

	public boolean check(String text) {
		if (text.startsWith("@")) {
			return true;
		}
		return false;
	}

	/**
	 * 解析调用表达式属性
	 */
	public Variable parse(String text) {
		text = text.trim();

		int left  = text.indexOf("(");
		int right = text.lastIndexOf(")");

		if (left == -1 || right == -1 || left > right) {
			//			throw new OgnlException("不合法的调用表达式: " + text);
			throw OgnlException.createLang("ognl.expr.n.illegal", "call", "text");
		}

		String   methodName = StringUtils.trimEmpty(text.substring(1, left));
		String   argString  = StringUtils.trimEmpty(text.substring(left + 1, right));
		Method   method     = getStaticMethod(methodName);
		Object[] vars       = null;

		if (null != argString) {
			List<String> list = XmlTextParseUtil.splitToList(argString, ',');
			if (null != list) {
				vars = new Object[list.size()];
				for (int i = 0; i < list.size(); i++) {
					vars[i] = XmlTextParseUtil.parseTextValue(list.get(i));
				}
			}
		}
		return new CallVariable(text, method, vars);
	}

	private Method getStaticMethod(String methodName) {
		Method m = XmlStaticMethodContainer.getStaticMethod(methodName);
		if (null == m) {
			XmlStaticMethodContainer.register(methodName);
			m = XmlStaticMethodContainer.getStaticMethod(methodName);
		}
		return m;
	}

}
