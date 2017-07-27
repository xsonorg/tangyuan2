package org.xson.tangyuan.type;

public enum Null {
	//
	BOOLEAN(BooleanTypeHandler.instance, JdbcType.BOOLEAN),
	//
	BYTE(ByteTypeHandler.instance, JdbcType.TINYINT),
	//
	SHORT(ShortTypeHandler.instance, JdbcType.SMALLINT),
	//
	INTEGER(IntegerTypeHandler.instance, JdbcType.INTEGER),
	//
	LONG(LongTypeHandler.instance, JdbcType.BIGINT),
	//
	FLOAT(FloatTypeHandler.instance, JdbcType.FLOAT),
	//
	DOUBLE(DoubleTypeHandler.instance, JdbcType.DOUBLE),
	//
	BIGDECIMAL(BigDecimalTypeHandler.instance, JdbcType.DECIMAL),
	//
	STRING(StringTypeHandler.instance, JdbcType.VARCHAR),
	//
	CLOB(ClobTypeHandler.instance, JdbcType.CLOB),
	//
	LONGVARCHAR(ClobTypeHandler.instance, JdbcType.LONGVARCHAR),
	//
	BYTEARRAY(ByteArrayTypeHandler.instance, JdbcType.LONGVARBINARY),
	//
	BLOB(BlobTypeHandler.instance, JdbcType.BLOB),
	//
	LONGVARBINARY(BlobTypeHandler.instance, JdbcType.LONGVARBINARY),
	//
	OBJECT(ObjectTypeHandler.instance, JdbcType.OTHER),
	//
	OTHER(ObjectTypeHandler.instance, JdbcType.OTHER),
	//
	TIMESTAMP(DateTypeHandler.instance, JdbcType.TIMESTAMP),
	//
	DATE(DateOnlyTypeHandler.instance, JdbcType.DATE),
	//
	TIME(TimeOnlyTypeHandler.instance, JdbcType.TIME),
	//
	SQLTIMESTAMP(SqlTimestampTypeHandler.instance, JdbcType.TIMESTAMP),
	//
	SQLDATE(SqlDateTypeHandler.instance, JdbcType.DATE),
	//
	SQLTIME(SqlTimeTypeHandler.instance, JdbcType.TIME);

	private TypeHandler<?>	typeHandler;
	private JdbcType		jdbcType;

	private Null(TypeHandler<?> typeHandler, JdbcType jdbcType) {
		this.typeHandler = typeHandler;
		this.jdbcType = jdbcType;
	}

	public TypeHandler<?> getTypeHandler() {
		return typeHandler;
	}

	public JdbcType getJdbcType() {
		return jdbcType;
	}
}
