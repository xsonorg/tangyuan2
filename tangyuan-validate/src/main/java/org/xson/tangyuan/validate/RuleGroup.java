package org.xson.tangyuan.validate;

import org.xson.common.object.XCO;

import java.util.List;

public class RuleGroup {

	private String				id		= null;
	private String				ns		= null;
	private List<RuleGroupItem>	items	= null;
	/** 描述 */
	private String				desc	= null;
	/** 错误信息 */
	private String				message	= null;
	/** 错误代码 */
	private int					code;

	private String[]			groups	= null;

	public RuleGroup(String id, String ns, List<RuleGroupItem> items, String desc, String message, int code, String[] groups) {
		this.id = id;
		this.ns = ns;
		this.items = items;
		this.desc = desc;
		this.message = message;
		this.code = code;
		this.groups = groups;
	}

	public String getId() {
		return id;
	}

	public String getNs() {
		return ns;
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

	public String[] getGroups() {
		return groups;
	}

	public boolean check(XCO xco, boolean forcedThrowException) {
		return check(xco, forcedThrowException, false);
	}

	public boolean check(XCO xco, boolean forcedThrowException, boolean ignoreDefaultValue) {
		boolean result = false;
		for (RuleGroupItem item : this.items) {
			result = item.check(xco, forcedThrowException, ignoreDefaultValue);
			if (!result) {
				break;
			}
		}
		return result;
	}

}
