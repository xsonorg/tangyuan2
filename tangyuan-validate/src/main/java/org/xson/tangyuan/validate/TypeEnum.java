package org.xson.tangyuan.validate;

public enum TypeEnum {
	// 1.
	INTEGER("int"),
	// 2.
	LONG("long"),
	// 3.
	FLOAT("float"),
	// 4.
	DOUBLE("double"),
	// 5.
	STRING("string"),
	// 6.
	DATE("date"),
	// 7.
	TIME("time"),
	// 9.
	DATETIME("dateTime"),
	//
	TIMESTAMP("timestamp"),
	// 10.
	BIGINTEGER("bigInteger"),
	// 11.
	BIGDECIMAL("bigDecimal"),
	// 12.
	ARRAY("array"),
	// 13.
	COLLECTION("collection");

	private String value;

	TypeEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static TypeEnum getEnum(String value) {
		TypeEnum[] all = values();
		for (TypeEnum typeEnum : all) {
			if (typeEnum.value.equalsIgnoreCase(value)) {
				return typeEnum;
			}
		}
		return null;
	}
}