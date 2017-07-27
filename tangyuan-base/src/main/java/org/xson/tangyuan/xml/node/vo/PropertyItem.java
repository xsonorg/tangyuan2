package org.xson.tangyuan.xml.node.vo;

/**
 * 属性项，支持Call, Return
 */
public class PropertyItem {

	public String	name;
	public Object	value;

	public PropertyItem(String name, Object value) {
		this.name = name;
		this.value = value;
	}
}
