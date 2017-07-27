package org.xson.tangyuan.ognl.vars.vo;

/**
 * 变量
 */
public class VariableItem {

	/**
	 * 属性表达式类型
	 */
	public enum VariableItemType {
		/**
		 * 属性
		 */
		PROPERTY,

		/**
		 * 索引
		 */
		INDEX,

		/**
		 * 变量:有可能是属性, 有可能是索引. 需要看所属的对象类型, 主要针对于括号中的
		 */
		VAR
	}

	/**
	 * 此属性的类型
	 */
	private VariableItemType	type;

	/**
	 * 属性名称
	 */
	private String				name;

	/**
	 * 索引
	 */
	private int					index;

	public VariableItem(String name, boolean isVar) {
		if (isVar) {
			this.type = VariableItemType.VAR;
		} else {
			this.type = VariableItemType.PROPERTY;
		}
		this.name = name;
	}

	public VariableItem(int index) {
		this.type = VariableItemType.INDEX;
		this.index = index;
	}

	public VariableItemType getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public int getIndex() {
		return index;
	}

}
