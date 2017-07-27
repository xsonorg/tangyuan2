package org.xson.tangyuan.ognl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldVoWrapper {

	private List<FieldVo>			fieldList;
	private Map<String, FieldVo>	fieldMap;

	public FieldVoWrapper(List<FieldVo> fieldList) {
		this.fieldList = fieldList;
		fieldMap = new HashMap<String, FieldVo>();
		for (FieldVo model : fieldList) {
			fieldMap.put(model.getName(), model);
		}
	}

	public List<FieldVo> getFieldList() {
		return fieldList;
	}

	public Map<String, FieldVo> getFieldMap() {
		return fieldMap;
	}

}
