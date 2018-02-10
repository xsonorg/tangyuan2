package org.xson.tangyuan.validate;

import org.xson.common.object.XCO;

import java.util.List;

public class RuleGroup {

	private String				id;
	private List<RuleGroupItem>	items;
	private String				desc;		// 描述
	private String				message;	// 错误信息
	private int					code;		// 错误代码

	public RuleGroup(String id, List<RuleGroupItem> items, String desc, String message, int code) {
		this.id = id;
		this.items = items;
		this.desc = desc;
		this.message = message;
		this.code = code;
	}

	public String getId() {
		return id;
	}

	protected List<RuleGroupItem> getItems() {
		return this.items;
	}

	public String getDesc() {
		return desc;
	}

	public String getMessage() {
		return message;
	}

	public int getCode() {
		return code;
	}

	public boolean check(XCO xco, boolean forcedThrowException) {
		boolean result = false;
		for (RuleGroupItem item : this.items) {
			result = item.check(xco, forcedThrowException);
			if (!result) {
				break;
			}
		}
		return result;
	}

}
