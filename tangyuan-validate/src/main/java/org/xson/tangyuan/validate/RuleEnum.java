package org.xson.tangyuan.validate;

public enum RuleEnum {

	// 1.
	ENUM("枚举值"),
	// 2.
	INTERVAL("区间值"),
	// 3.
	FILTER("过滤"),
	// 4.
	MAX_LENGTH("最大长度"),
	// 5.
	MIN_LENGTH("最小长度"),
	// 6.
	INTERVAL_LENGTH("区间长度"),
	// 7.
	MATCH("匹配"),
	// 8.
	MISMATCH("不匹配"),
	// 9.
	MIN("最小值"),
	// 10.
	MAX("最大值");

	private String	value;

	RuleEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static RuleEnum getEnum(String value) {
		RuleEnum[] all = values();
		for (RuleEnum ruleEnum : all) {
			if (ruleEnum.value.equalsIgnoreCase(value)) {
				return ruleEnum;
			}
		}
		return null;
	}
}
