package org.xson.tangyuan.service.mongo.sql;

import org.xson.tangyuan.service.mongo.sql.UpdateVo.ColumnUpdateType;

public class ColumnUpdateVo {

	private String				name;

	private ValueVo				valueVo;

	private ColumnUpdateType	type;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ValueVo getValueVo() {
		return valueVo;
	}

	public void setValueVo(ValueVo valueVo) {
		this.valueVo = valueVo;
	}

	public ColumnUpdateType getType() {
		return type;
	}

	public void setType(ColumnUpdateType type) {
		this.type = type;
	}

}
