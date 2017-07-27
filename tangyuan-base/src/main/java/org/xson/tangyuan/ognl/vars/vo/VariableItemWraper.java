package org.xson.tangyuan.ognl.vars.vo;

import java.util.List;

import org.xson.tangyuan.ognl.Ognl;
import org.xson.tangyuan.ognl.vars.Variable;

/**
 * 变量单元(包装,可能包含多个Item)
 */
public class VariableItemWraper extends Variable {

	private VariableItem		item		= null;

	private List<VariableItem>	itemList	= null;

	public VariableItemWraper(String original, VariableItem item) {
		this.original = original;
		this.item = item;
	}

	public VariableItemWraper(String original, List<VariableItem> itemList) {
		this.original = original;
		this.itemList = itemList;
	}

	public VariableItem getItem() {
		return item;
	}

	public List<VariableItem> getItemList() {
		return itemList;
	}

	public Object getValue(Object arg) {
		return Ognl.getValue(arg, this);
	}
}
