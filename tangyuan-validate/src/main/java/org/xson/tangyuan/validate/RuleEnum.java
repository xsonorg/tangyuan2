package org.xson.tangyuan.validate;

public enum RuleEnum {
	// 1.
	ENUM("枚举值", "enum"),
	// 2.
	INTERVAL("区间值", "interval"),
	// 3.
	// FILTER("过滤"),
	CHECK("检查", "check"),
	// 4.
	MAX_LENGTH("最大长度", "max_length"),
	// 5.
	MIN_LENGTH("最小长度", "min_length"),
	// 6.
	INTERVAL_LENGTH("区间长度", "interval_length"),
	// 7.
	MATCH("匹配", "match"),
	// 8.
	// MISMATCH("不匹配"),
	UNMATCH("不匹配", "unmatch"),
	// 9.
	MIN("最小值", "min"),
	// 10.
	MAX("最大值", "max");

	private String	cnValue;
	private String	enValue;

	RuleEnum(String cnValue, String enValue) {
		this.cnValue = cnValue;
		this.enValue = enValue;
	}

	public String getCnValue() {
		return cnValue;
	}

	public String getEnValue() {
		return enValue;
	}

	public static RuleEnum getEnum(String value) {
		RuleEnum[] all = values();
		for (RuleEnum ruleEnum : all) {
			if (ruleEnum.cnValue.equalsIgnoreCase(value) || ruleEnum.enValue.equalsIgnoreCase(value)) {
				return ruleEnum;
			}
		}
		return null;
	}
}
