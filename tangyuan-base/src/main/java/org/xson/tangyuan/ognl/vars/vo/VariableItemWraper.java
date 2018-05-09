package org.xson.tangyuan.ognl.vars.vo;

import java.util.List;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.ognl.Ognl;
import org.xson.tangyuan.ognl.vars.Variable;

/**
 * 变量单元(包装,可能包含多个Item)
 */
public class VariableItemWraper extends Variable {

	private VariableItem		item			= null;

	private List<VariableItem>	itemList		= null;

	private String				extArgPrefix	= null;

	//	public VariableItemWraper(String original, VariableItem item) {
	//		this.original = original;
	//		this.item = item;
	//	}
	//
	//	public VariableItemWraper(String original, List<VariableItem> itemList) {
	//		this.original = original;
	//		this.itemList = itemList;
	//	}

	public VariableItemWraper(String original, String extArgPrefix, VariableItem item) {
		this.original = original;
		this.item = item;
		this.extArgPrefix = extArgPrefix;
	}

	public VariableItemWraper(String original, String extArgPrefix, List<VariableItem> itemList) {
		this.original = original;
		this.itemList = itemList;
		this.extArgPrefix = extArgPrefix;
	}

	public VariableItem getItem() {
		return item;
	}

	public List<VariableItem> getItemList() {
		return itemList;
	}

	public Object getValue(Object arg) {
		//		return Ognl.getValue(arg, this);
		if (null == this.extArgPrefix) {
			return Ognl.getValue(arg, this);
		} else {
			Object extArg = TangYuanContainer.getInstance().getExtArg().getArg(this.extArgPrefix);
			return Ognl.getValue(extArg, this);
		}
	}
}
