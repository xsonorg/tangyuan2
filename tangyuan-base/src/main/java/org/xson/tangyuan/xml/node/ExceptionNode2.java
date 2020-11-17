package org.xson.tangyuan.xml.node;

import java.util.ArrayList;
import java.util.List;

import org.xson.tangyuan.executor.ServiceException;
import org.xson.tangyuan.ognl.vars.ArgSelfVo;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.ognl.vars.vo.LogicalVariable;
import org.xson.tangyuan.ognl.vars.warper.GAParserWarper;
import org.xson.tangyuan.service.ActuatorContext;

public class ExceptionNode2 implements TangYuanNode {

	private LogicalVariable test;

	private int             code;
	private String          message;

	private List<Object>    unitList;

	public ExceptionNode2(LogicalVariable test, int code, String message, String i18n) {
		this.test = test;
		this.code = code;
		this.message = message;
		if (null != this.message) {
			//			this.unitList = new TokenParserUtil().parseLog(this.message, "{", "}");
			this.unitList = parseLog(this.message, "{", "}");
		}
	}

	@Override
	public boolean execute(ActuatorContext ac, Object arg, Object acArg) throws Throwable {
		if (test.getResult(acArg)) {
			String errorMessage = this.message;
			if (null != unitList) {
				errorMessage = getErrorMessage(arg, acArg);
			}
			throw new ServiceException(code, (null != errorMessage) ? errorMessage : "");
		}
		return true;
	}

	private String getErrorMessage(Object arg, Object acArg) {
		StringBuilder builder = new StringBuilder();
		for (Object obj : unitList) {
			if (String.class == obj.getClass()) {
				builder.append(obj);
			} else if (obj instanceof Variable) {
				Variable varVo = (Variable) obj;
				Object   value = varVo.getValue(acArg);
				if (null == value && ArgSelfVo.AEG_SELF_MARK.equalsIgnoreCase(varVo.getOriginal())) {
					value = arg.toString();
				}
				builder.append((null != value) ? value.toString() : "null");
			} else {
				builder.append(obj.toString());
			}
		}
		return builder.toString();
	}

	//	private String getErrorMessage(Object arg) {
	//		StringBuilder builder = new StringBuilder();
	//		for (Object obj : unitList) {
	//			if (String.class == obj.getClass()) {
	//				builder.append(obj);
	//			} else if (obj instanceof Variable) {
	//				Variable varVo = (Variable) obj;
	//				Object   value = varVo.getValue(arg);
	//				if (null == value && ArgSelfVo.AEG_SELF_MARK.equalsIgnoreCase(varVo.getOriginal())) {
	//					value = arg.toString();
	//				}
	//				builder.append((null != value) ? value.toString() : "null");
	//			} else {
	//				builder.append(obj.toString());
	//			}
	//		}
	//		return builder.toString();
	//	}

	public List<Object> parseLog(String text, String openToken, String closeToken) {
		boolean       hasToken = false;
		StringBuilder builder  = new StringBuilder();
		List<Object>  unitlist = new ArrayList<>();
		if (text != null && text.length() > 0) {
			char[] src    = text.toCharArray();
			int    offset = 0;
			int    start  = text.indexOf(openToken, offset);
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
						// builder.append(handler.handleToken(content));
						// unitlist.add(VariableParser.parse(content, false));
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

	//	@Override
	//	public boolean execute(ServiceContext context, Object arg) {
	//		if (test.getResult(arg)) {
	//			String errorMessage = this.message;
	//			if (null != unitList) {
	//				errorMessage = getErrorMessage(arg);
	//			}
	//			// throw new ServiceException(code, (null != message) ? message : "");
	//			throw new ServiceException(code, (null != errorMessage) ? errorMessage : "");
	//		}
	//		return true;
	//	}
}
