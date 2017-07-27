package org.xson.tangyuan.validate;

import org.xson.common.object.XCO;

import java.util.List;

public class RuleGroup {

	private String				id;
	private List<RuleGroupItem>	items;
	private String				desc;		// 描述
	private String				message;	// 错误信息

	public RuleGroup(String id, List<RuleGroupItem> items, String desc, String message) {
		this.id = id;
		this.items = items;
		this.desc = desc;
		this.message = message;
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

	public boolean check(XCO xco) {

		boolean result = false;

		for (RuleGroupItem item : this.items) {
			result = item.check(xco);
			if (!result) {
				break;
			}
		}

		if (!result && ValidateComponent.getInstance().isThrowException()) {
			throw new XCOValidateException(ValidateComponent.getInstance().getErrorCode(),
					(null != this.message) ? this.message : ValidateComponent.getInstance().getErrorMessage());
		}

		return result;
	}

}
