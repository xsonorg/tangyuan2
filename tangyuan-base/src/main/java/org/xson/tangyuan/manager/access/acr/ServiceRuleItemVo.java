package org.xson.tangyuan.manager.access.acr;

public class ServiceRuleItemVo extends AppRuleItemVo {

	public enum ServiceMatchType {
		PRECISE, FUZZY;

		public static ServiceMatchType parse(String service) {
			if (null == service) {
				return null;
			}
			if (service.indexOf("*") > -1) {
				return FUZZY;
			} else {
				return PRECISE;
			}
		}
	}

	protected String           service;
	protected ServiceMatchType serviceMatchType;

	//	public void init(RuleType ruleType, String value, String service) {
	//		super.init(ruleType, value);
	//		this.service = service;
	//		this.serviceMatchType = ServiceMatchType.parse(service);
	//	}

	public ServiceRuleItemVo(RuleType ruleType, String value, MatchType matchType, String service, ServiceMatchType serviceMatchType) {
		super(ruleType, value, matchType);
		this.service = service;
		this.serviceMatchType = serviceMatchType;
	}

	public String getService() {
		return service;
	}

	public ServiceMatchType getServiceMatchType() {
		return serviceMatchType;
	}
}
