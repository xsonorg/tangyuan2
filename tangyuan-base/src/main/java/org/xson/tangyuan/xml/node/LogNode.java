package org.xson.tangyuan.xml.node;

import java.util.ArrayList;
import java.util.List;

import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.ognl.vars.ArgSelfVo;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.ognl.vars.warper.GAParserWarper;
import org.xson.tangyuan.service.ActuatorContext;

public class LogNode implements TangYuanNode {

	private static Log		log				= LogFactory.getLog(LogNode.class);

	/**
	 * 5:ERROR, 4:WARN, 3:INFO, 2:DEBUG, 1:TRACE
	 */
	private int				level;

	/**
	 * 这是{a}一条{b}日志
	 */
	private String			originalText	= null;

	/**
	 * 里边包含字符串和动态变量
	 */
	private List<Object>	logUnits		= null;

	public LogNode(int level, String text) {
		this.level = level;
		this.originalText = text;
		this.logUnits = parseLog(text, "{", "}");
	}

	@Override
	public boolean execute(ActuatorContext ac, Object arg, Object acArg) {
		String parsedText = this.originalText;
		if (null != logUnits) {
			StringBuilder builder = new StringBuilder();
			for (Object obj : logUnits) {
				// 增加特殊日志打印
				if (String.class == obj.getClass()) {
					builder.append(obj);
				} else {
					Variable varVo = (Variable) obj;
					Object value = varVo.getValue(acArg);
					if (null == value && ArgSelfVo.AEG_SELF_MARK.equalsIgnoreCase(varVo.getOriginal())) {
						value = arg.toString();
					}
					builder.append((null != value) ? value.toString() : "null");
				}
			}
			parsedText = builder.toString();
		}

		if (5 == this.level) {
			log.error(parsedText);
		} else if (4 == this.level) {
			log.warn(parsedText);
		} else if (3 == this.level) {
			log.info(parsedText);
		} else if (2 == this.level) {
			log.debug(parsedText);
		} else {
			log.trace(parsedText);
		}
		return true;
	}

	public List<Object> parseLog(String text, String openToken, String closeToken) {
		boolean hasToken = false;
		StringBuilder builder = new StringBuilder();
		List<Object> unitlist = new ArrayList<>();
		if (text != null && text.length() > 0) {
			char[] src = text.toCharArray();
			int offset = 0;
			int start = text.indexOf(openToken, offset);
			while (start > -1) {
				if (start > 0 && src[start - 1] == '\\') {
					// the variable is escaped. remove the backslash.
					builder.append(src, offset, start - 1).append(openToken);
					offset = start + openToken.length();
				} else {
					int end = text.indexOf(closeToken, start);
					if (end == -1) {
						builder.append(src, offset, src.length - offset);
						offset = src.length;
					} else {
						builder.append(src, offset, start - offset);
						unitlist.add(builder.toString());
						builder = new StringBuilder();

						offset = start + openToken.length();
						String content = new String(src, offset, end - offset);
						unitlist.add(new GAParserWarper().parse(content));

						offset = end + closeToken.length();
						hasToken = true;
					}
				}
				start = text.indexOf(openToken, offset);
			}
			if (offset < src.length) {
				builder.append(src, offset, src.length - offset);
				unitlist.add(builder.toString());
			}
		}
		if (hasToken) {
			return unitlist;
		}
		return null;
	}
}
