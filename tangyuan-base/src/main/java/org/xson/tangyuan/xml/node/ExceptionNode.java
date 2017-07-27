package org.xson.tangyuan.xml.node;

import java.util.List;

import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.executor.ServiceException;
import org.xson.tangyuan.ognl.vars.ArgSelfVo;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.ognl.vars.vo.LogicalVariable;
import org.xson.tangyuan.util.TokenParserUtil;

public class ExceptionNode implements TangYuanNode {

	private LogicalVariable	test;

	private int				code;

	private String			message;

	private List<Object>	unitList;

	// private String i18n;
	// this.i18n = i18n;
	// if (null == i18n) {
	// this.i18n = this.message;
	// }

	public ExceptionNode(LogicalVariable test, int code, String message, String i18n) {
		this.test = test;
		this.code = code;
		this.message = message;
		// this.i18n = i18n;
		if (null != this.message) {
			this.unitList = new TokenParserUtil().parseLog(this.message, "{", "}");
		}
	}

	@Override
	public boolean execute(ServiceContext context, Object arg) {
		if (test.getResult(arg)) {
			String errorMessage = this.message;
			if (null != unitList) {
				errorMessage = getErrorMessage(arg);
			}
			// throw new ServiceException(code, (null != message) ? message : "");
			throw new ServiceException(code, (null != errorMessage) ? errorMessage : "");
		}
		return true;
	}

	private String getErrorMessage(Object arg) {
		StringBuilder builder = new StringBuilder();
		for (Object obj : unitList) {
			if (String.class == obj.getClass()) {
				builder.append(obj);
			} else if (obj instanceof Variable) {
				Variable varVo = (Variable) obj;
				Object value = varVo.getValue(arg);
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
}
