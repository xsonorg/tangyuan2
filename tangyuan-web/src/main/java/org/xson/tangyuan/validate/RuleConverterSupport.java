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
		List<RuleGroupItem> items = getRuleList(cVo);
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
			// TypeEnum type = item.getType();
			// List<RuleGroupItem> children = item.getItems();
			// if (CollectionUtils.isEmpty(children)) {
			// setXCOValue(arg, fieldName, type, value);
			// }
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

}
