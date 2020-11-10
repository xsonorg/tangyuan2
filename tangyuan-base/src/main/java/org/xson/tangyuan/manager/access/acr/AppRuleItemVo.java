package org.xson.tangyuan.manager.access.acr;

public class AppRuleItemVo {

	public enum RuleType {
		BLACK_DOMAIN, BLACK_IP, WHITE_DOMAIN, WHITE_IP;

		public static RuleType parse(String value) {
			if ("域名黑名单".equalsIgnoreCase(value) || "BLACK_DOMAIN".equalsIgnoreCase(value)) {
				return BLACK_DOMAIN;
			} else if ("IP黑名单".equalsIgnoreCase(value) || "BLACK_IP".equalsIgnoreCase(value)) {
				return BLACK_IP;
			} else if ("域名白名单".equalsIgnoreCase(value) || "WHITE_DOMAIN".equalsIgnoreCase(value)) {
				return WHITE_DOMAIN;
			} else if ("IP白名单".equalsIgnoreCase(value) || "WHITE_IP".equalsIgnoreCase(value)) {
				return WHITE_IP;
			}
			return null;
		}
	}

	public enum MatchType {
		ALL, PRECISE, FUZZY, FUZZY1;

		public static MatchType parse(String value) {
			if (null == value) {
				return null;
			}
			if ("*".equals(value)) {
				return ALL;
			} else if (value.indexOf("*") > -1) {
				return FUZZY;
			} else if (value.indexOf("/") > -1) {
				return FUZZY1;
			} else if ("*".equals(value)) {
				return PRECISE;
			}
			return null;
		}
	}

	protected RuleType  ruleType;
	protected String    value;
	protected MatchType matchType;
	//	private RuleObjectType ruleObjectType;

	public AppRuleItemVo(RuleType ruleType, String value, MatchType matchType) {
		this.ruleType = ruleType;
		this.value = value;
		//		this.matchType = MatchType.parse(value);
		this.matchType = matchType;
	}

	public RuleType getRuleType() {
		return ruleType;
	}

	public String getValue() {
		return value;
	}

	public MatchType getMatchType() {
		return matchType;
	}

}
