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
	// 8.
	TIMESTAMP("timestamp"),
	// 9.
	DATETIME("dateTime"),
	// 10.
	BIGINTEGER("bigInteger"),
	// 11.
	BIGDECIMAL("bigDecimal"),
	// 12.
	ARRAY("array"),
	// 13.
	COLLECTION("collection"),

	// * add new

	XCO("xco"),

	BYTE("byte"),

	BOOLEAN("boolean"),

	SHORT("short"),

	CHAR("char"),

	// array

	INT_ARRAY("int_array"),

	LONG_ARRAY("long_array"),

	FLOAT_ARRAY("float_array"),

	DOUBLE_ARRAY("double_array"),

	BYTE_ARRAY("byte_array"),

	BOOLEAN_ARRAY("boolean_array"),

	SHORT_ARRAY("short_array"),

	CHAR_ARRAY("char_array"),

	STRING_ARRAY("string_array"),

	XCO_ARRAY("xco_array"),

	// set

	STRING_LIST("string_list"),

	XCO_LIST("xco_list"),

	STRING_SET("string_set"),

	XCO_SET("xco_set");

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