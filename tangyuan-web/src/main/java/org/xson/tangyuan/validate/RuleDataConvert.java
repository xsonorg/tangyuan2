package org.xson.tangyuan.validate;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.xson.common.object.XCO;
import org.xson.common.object.XCOUtil;
import org.xson.tangyuan.util.StringUtils;

public class RuleDataConvert {

	public static XCO convert(HttpServletRequest request, String ruleGroupId) {
		RuleGroup group = ValidateComponent.getInstance().ruleGroupsMap.get(ruleGroupId);
		if (group == null) {
			throw new XCOValidateException("validation template does not exist: " + ruleGroupId);
		}
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
		return xco;
	}

	private static void setXCOValue(XCO xco, String fieldName, TypeEnum type, String value) {
		if (type == TypeEnum.INTEGER) {
			xco.setIntegerValue(fieldName, Integer.parseInt(value));
		} else if (type == TypeEnum.LONG) {
			xco.setLongValue(fieldName, Long.parseLong(value));
		} else if (type == TypeEnum.FLOAT) {
			xco.setFloatValue(fieldName, Float.parseFloat(value));
		} else if (type == TypeEnum.DOUBLE) {
			xco.setDoubleValue(fieldName, Double.parseDouble(value));
		} else if (type == TypeEnum.STRING) {
			xco.setStringValue(fieldName, value);
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
		}
		// 其他类型不做转换
	}

}
