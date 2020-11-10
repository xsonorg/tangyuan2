package org.xson.tangyuan.manager.access.acr;

import java.util.List;

public class ServiceRuleSet extends AppRuleSet {

	protected String service;

	public void init(List<AppRuleItemVo> list, String service) {
		super.init(list);
		this.service = service;
	}

	public String getService() {
		return service;
	}

}
