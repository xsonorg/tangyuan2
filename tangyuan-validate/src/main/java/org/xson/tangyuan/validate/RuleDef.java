package org.xson.tangyuan.validate;

import java.util.List;

public class RuleDef {

	private String		id;

	private List<Rule>	rule;

	public RuleDef(String id, List<Rule> rule) {
		this.id = id;
		this.rule = rule;
	}

	public String getId() {
		return id;
	}

	public List<Rule> getRule() {
		return rule;
	}
}
