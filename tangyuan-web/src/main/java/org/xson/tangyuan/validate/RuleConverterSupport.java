package org.xson.tangyuan.validate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.xson.common.object.XCO;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.util.CollectionUtils;
import org.xson.tangyuan.web.ControllerVo;

import com.alibaba.fastjson.JSONObject;

public class RuleConverterSupport {

	private JSONFieldConverterSupport jsonFieldConverterSupport = JSONFieldConverterSupport.getInstance();

	public List<RuleGroupItem> getRuleList(ControllerVo cVo) {
		String ruleGroupId = cVo.getValidate();
		if (null == ruleGroupId) {
			throw new XCOValidateException(TangYuanLang.get("web.converter.ruleGroup.id.invalid", "Null", cVo.getUrl()));
		}
		RuleGroup group = ValidateComponent.getInstance().getRuleGroup(ruleGroupId);
		if (group == null) {
			throw new XCOValidateException(TangYuanLang.get("web.converter.ruleGroup.id.invalid", ruleGroupId, cVo.getUrl()));
		}
		List<RuleGroupItem> items = group.getItems();
		return items;
	}

	public Map<String, RuleGroupItem> getRuleMap(ControllerVo cVo) {
		List<RuleGroupItem>        items   = getRuleList(cVo);
		Map<String, RuleGroupItem> ruleMap = new HashMap<String, RuleGroupItem>();
		for (RuleGroupItem item : items) {
			ruleMap.put(item.getFieldName(), item);
		}
		return ruleMap;
	}

	public void convertFromRequest(XCO arg, ControllerVo cVo, HttpServletRequest request) throws Throwable {
		List<RuleGroupItem> items = getRuleList(cVo);
		// 从rule角度进行转换, 未在rule定义的参数将被忽略
		convertFromRequest0(arg, items, request);
	}

	private void convertFromRequest0(XCO arg, List<RuleGroupItem> items, HttpServletRequest request) throws Throwable {
		for (RuleGroupItem item : items) {
			String fieldName = item.getFieldName();
			if (null == fieldName) {
				continue;
			}
			String value = request.getParameter(fieldName);
			if (null == value) {
				continue;
			}
			this.jsonFieldConverterSupport.converterJSONField(item, arg, value);
		}
	}

	public void convertFromMap(XCO arg, ControllerVo cVo, Map<String, String> data) throws Throwable {
		List<RuleGroupItem> items = getRuleList(cVo);
		if (CollectionUtils.isEmpty(data)) {
			return;
		}
		// 从rule角度进行转换, 未在rule定义的参数将被忽略
		convertFromMap0(arg, items, data);
	}

	private void convertFromMap0(XCO arg, List<RuleGroupItem> items, Map<String, String> data) throws Throwable {
		for (RuleGroupItem item : items) {
			String fieldName = item.getFieldName();
			if (null == fieldName) {
				continue;
			}
			String value = data.get(fieldName);
			if (null == value) {
				continue;
			}
			this.jsonFieldConverterSupport.converterJSONField(item, arg, value);
			//			TypeEnum            type     = item.getType();
			//			List<RuleGroupItem> children = item.getItems();
			//			if (CollectionUtils.isEmpty(children)) {
			//				setXCOValue(arg, fieldName, type, value);
			//			}
		}
	}

	public void convertFromJSON(XCO arg, ControllerVo cVo, JSONObject data) throws Throwable {
		List<RuleGroupItem> items = getRuleList(cVo);
		// 从rule角度进行转换, 未在rule定义的参数将被忽略
		convertFromJSON0(arg, items, data);
	}

	private void convertFromJSON0(XCO arg, List<RuleGroupItem> items, JSONObject data) throws Throwable {
		if (data.isEmpty()) {
			return;
		}
		for (RuleGroupItem item : items) {
			String fieldName = item.getFieldName();
			if (null == fieldName) {
				continue;
			}
			Object value = data.get(fieldName);
			if (null == value) {
				continue;
			}
			this.jsonFieldConverterSupport.converterJSONField(item, arg, value);
		}
	}

	//	/**
	//	 * 支持单层的，简单类型的转换
	//	 */
	//	public void setXCOValue(XCO xco, String fieldName, TypeEnum type, String value) {
	//		if (type == TypeEnum.STRING || type == null) {
	//			xco.setStringValue(fieldName, value);
	//			return;
	//		}
	//
	//		if (type == TypeEnum.INTEGER) {
	//			xco.setIntegerValue(fieldName, Integer.parseInt(value));
	//		} else if (type == TypeEnum.LONG) {
	//			xco.setLongValue(fieldName, Long.parseLong(value));
	//		} else if (type == TypeEnum.FLOAT) {
	//			xco.setFloatValue(fieldName, Float.parseFloat(value));
	//		} else if (type == TypeEnum.DOUBLE) {
	//			xco.setDoubleValue(fieldName, Double.parseDouble(value));
	//		} else if (type == TypeEnum.BIGINTEGER) {
	//			xco.setBigIntegerValue(fieldName, new BigInteger(value));
	//		} else if (type == TypeEnum.BIGDECIMAL) {
	//			xco.setBigDecimalValue(fieldName, new BigDecimal(value));
	//		} else if (type == TypeEnum.DATETIME) {
	//			xco.setDateTimeValue(fieldName, XCOUtil.parseDateTime(value));
	//		} else if (type == TypeEnum.DATE) {
	//			xco.setDateValue(fieldName, XCOUtil.parseDate(value));
	//		} else if (type == TypeEnum.TIME) {
	//			xco.setTimeValue(fieldName, XCOUtil.parseTime(value));
	//		} else if (type == TypeEnum.TIMESTAMP) {
	//			xco.setTimestampValue(fieldName, XCOUtil.parseTimestamp(value));
	//		} else if (type == TypeEnum.BYTE) {
	//			xco.setByteValue(fieldName, Byte.parseByte(value));
	//		} else if (type == TypeEnum.BOOLEAN) {
	//			xco.setBooleanValue(fieldName, Boolean.parseBoolean(value));
	//		} else if (type == TypeEnum.SHORT) {
	//			xco.setShortValue(fieldName, Short.parseShort(value));
	//		} else if (type == TypeEnum.CHAR) {
	//			xco.setCharValue(fieldName, value.charAt(0));
	//		} else {
	//			// 其他作为字符串处理
	//			xco.setStringValue(fieldName, value);
	//		}
	//	}

}
