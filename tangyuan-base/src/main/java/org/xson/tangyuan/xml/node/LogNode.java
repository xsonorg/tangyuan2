package org.xson.tangyuan.xml.node;

import java.util.List;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.ognl.vars.ArgSelfVo;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.util.TokenParserUtil;

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
		this.logUnits = new TokenParserUtil().parseLog(text, "{", "}");
	}

	@Override
	public boolean execute(ServiceContext context, Object arg) {
		String parsedText = this.originalText;
		if (null != logUnits) {
			StringBuilder builder = new StringBuilder();
			for (Object obj : logUnits) {
				// if (String.class == obj.getClass()) {
				// builder.append(obj);
				// } else {
				// Object value = ((VariableVo) obj).getValue(arg);
				// builder.append((null != value) ? value.toString() : "null");
				// }
				// 增加特殊日志打印
				if (String.class == obj.getClass()) {
					builder.append(obj);
				} else {
					Variable varVo = (Variable) obj;
					Object value = varVo.getValue(arg);
					// if (null == value && LOG_SPECIAL_MARK.equalsIgnoreCase(varVo.getOriginal())) {
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
}
