package org.xson.tangyuan.type;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

public enum JdbcType {
	/*
	 * This is added to enable basic support for the ARRAY data type - but a
	 * custom type handler is still required
	 */
	ARRAY(Types.ARRAY),
	//
	BIT(Types.BIT),
	//
	TINYINT(Types.TINYINT),
	//
	SMALLINT(Types.SMALLINT),
	//
	INTEGER(Types.INTEGER),
	//
	BIGINT(Types.BIGINT),
	//
	FLOAT(Types.FLOAT),
	//
	REAL(Types.REAL),
	//
	DOUBLE(Types.DOUBLE),
	//
	NUMERIC(Types.NUMERIC),
	//
	DECIMAL(Types.DECIMAL),
	//
	CHAR(Types.CHAR),
	//
	VARCHAR(Types.VARCHAR),
	//
	LONGVARCHAR(Types.LONGVARCHAR),
	//
	DATE(Types.DATE),
	//
	TIME(Types.TIME),
	//
	TIMESTAMP(Types.TIMESTAMP),
	//
	BINARY(Types.BINARY),
	//
	VARBINARY(Types.VARBINARY),
	//
	LONGVARBINARY(Types.LONGVARBINARY),
	//
	NULL(Types.NULL),
	//
	OTHER(Types.OTHER),
	//
	BLOB(Types.BLOB),
	//
	CLOB(Types.CLOB),
	//
	BOOLEAN(Types.BOOLEAN),
	// Oracle
	CURSOR(-10),
	//
	UNDEFINED(Integer.MIN_VALUE + 1000),
	// JDK6
	NVARCHAR(Types.NVARCHAR),
	// JDK6
	NCHAR(Types.NCHAR),
	// JDK6
	NCLOB(Types.NCLOB),
	//
	STRUCT(Types.STRUCT);

	public final int						TYPE_CODE;
	private static Map<Integer, JdbcType>	codeLookup	= new HashMap<Integer, JdbcType>();

	static {
		for (JdbcType type : JdbcType.values()) {
			codeLookup.put(type.TYPE_CODE, type);
		}
	}

	JdbcType(int code) {
		this.TYPE_CODE = code;
	}

	public static JdbcType forCode(int code) {
		return codeLookup.get(code);
	}

}
