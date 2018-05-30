package org.xson.tangyuan.validate;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.xson.common.object.XCO;
import org.xson.common.object.XCOUtil;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.web.DataConverter;
import org.xson.tangyuan.web.RequestContext;
import org.xson.tangyuan.web.xml.vo.ControllerVo;

public class RuleDataConverter implements DataConverter {

	public final static RuleDataConverter instance = new RuleDataConverter();

	@Override
	public void convert(RequestContext requestContext, ControllerVo cVo) throws Throwable {
		String ruleGroupId = cVo.getValidate();
		if (null == ruleGroupId) {
			throw new XCOValidateException("When using the rules for data conversion need to specify a template. uri: " + cVo.getUrl());
		}
		RuleGroup group = ValidateComponent.getInstance().ruleGroupsMap.get(ruleGroupId);
		if (group == null) {
			throw new XCOValidateException("The validation template required for data conversion does not exist: " + ruleGroupId);
		}
		HttpServletRequest request = requestContext.getRequest();
		XCO xco = new XCO();
		List<RuleGroupItem> items = group.getItems();
		for (RuleGroupItem item : items) {
			String fieldName = item.getFieldName();
			if (null != fieldName) {
				String tmp = StringUtils.trim(request.getParameter(fieldName));
				if (null != tmp) {
					setXCOValue(xco, fieldName, item.getType(), tmp);
				}
			}
		}
		XCO old = (XCO) requestContext.getArg();
		if (null == old) {
			requestContext.setArg(xco);
		} else {
			old.append(xco);
			requestContext.setArg(old);
		}
	}

	private void setXCOValue(XCO xco, String fieldName, TypeEnum type, String value) {
		if (type == TypeEnum.STRING) {
			xco.setStringValue(fieldName, value);
			return;
		}

		if (0 == value.length()) {
			throw new XCOValidateException("Data conversion error, field [" + fieldName + "] value is empty.");
		}

		if (type == TypeEnum.INTEGER) {
			xco.setIntegerValue(fieldName, Integer.parseInt(value));
		} else if (type == TypeEnum.LONG) {
			xco.setLongValue(fieldName, Long.parseLong(value));
		} else if (type == TypeEnum.FLOAT) {
			xco.setFloatValue(fieldName, Float.parseFloat(value));
		} else if (type == TypeEnum.DOUBLE) {
			xco.setDoubleValue(fieldName, Double.parseDouble(value));
		} else if (type == TypeEnum.BIGINTEGER) {
			xco.setBigIntegerValue(fieldName, new BigInteger(value));
		} else if (type == TypeEnum.BIGDECIMAL) {
			xco.setBigDecimalValue(fieldName, new BigDecimal(value));
		} else if (type == TypeEnum.DATETIME) {
			xco.setDateTimeValue(fieldName, XCOUtil.parseDateTime(value));
		} else if (type == TypeEnum.DATE) {
			xco.setDateValue(fieldName, XCOUtil.parseDate(value));
		} else if (type == TypeEnum.TIME) {
			xco.setTimeValue(fieldName, XCOUtil.parseTime(value));
		} else if (type == TypeEnum.TIMESTAMP) {
			xco.setTimestampValue(fieldName, XCOUtil.parseTimestamp(value));
		} else if (type == TypeEnum.BYTE) {
			xco.setByteValue(fieldName, Byte.parseByte(value));
		} else if (type == TypeEnum.BOOLEAN) {
			xco.setBooleanValue(fieldName, Boolean.parseBoolean(value));
		} else if (type == TypeEnum.SHORT) {
			xco.setShortValue(fieldName, Short.parseShort(value));
		} else if (type == TypeEnum.CHAR) {
			xco.setCharValue(fieldName, value.charAt(0));
		}
		// 其他类型不做转换
	}

	public XCO convertRESTURI(Map<String, String> queryMap, ControllerVo cVo) {

		String ruleGroupId = cVo.getValidate();
		if (null == ruleGroupId) {
			throw new XCOValidateException("When using the rules for data conversion need to specify a template. uri: " + cVo.getUrl());
		}

		RuleGroup group = ValidateComponent.getInstance().ruleGroupsMap.get(ruleGroupId);
		if (group == null) {
			throw new XCOValidateException("The validation template required for data conversion does not exist: " + ruleGroupId);
		}

		XCO xco = new XCO();
		List<RuleGroupItem> items = group.getItems();
		for (RuleGroupItem item : items) {
			String fieldName = item.getFieldName();
			if (null != fieldName) {
				String tmp = StringUtils.trim(queryMap.get(fieldName));
				if (null != tmp) {
					setXCOValue(xco, fieldName, item.getType(), tmp);
				}
			}
		}

		return xco;
	}
}
