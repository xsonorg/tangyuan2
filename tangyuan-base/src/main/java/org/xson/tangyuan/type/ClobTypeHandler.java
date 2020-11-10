package org.xson.tangyuan.type;

import java.io.StringReader;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.xson.common.object.XCO;
import org.xson.tangyuan.mapping.ColumnValueHandler;

public class ClobTypeHandler extends BaseTypeHandler<String> {

	public final static ClobTypeHandler instance = new ClobTypeHandler();

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
		StringReader reader = new StringReader(parameter);
		ps.setCharacterStream(i, reader, parameter.length());
	}

	@Override
	public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
		String value = "";
		Clob clob = rs.getClob(columnName);
		if (clob != null) {
			int size = (int) clob.length();
			value = clob.getSubString(1, size);
		}
		return value;
	}

	@Override
	public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		String value = "";
		Clob clob = rs.getClob(columnIndex);
		if (clob != null) {
			int size = (int) clob.length();
			value = clob.getSubString(1, size);
		}
		return value;
	}

	@Override
	public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		String value = "";
		Clob clob = cs.getClob(columnIndex);
		if (clob != null) {
			int size = (int) clob.length();
			value = clob.getSubString(1, size);
		}
		return value;
	}

	@Override
	public void appendLog(StringBuilder builder, String parameter, DatabaseDialect dialect) {
		// if (DatabaseDialect.MYSQL == dialect) {
		// builder.append('\'');
		// builder.append(replace(parameter));
		// builder.append('\'');
		// }
		builder.append('\'');
		builder.append(replace(parameter));
		builder.append('\'');
	}

	@Override
	public void setResultToXCO(ResultSet rs, String columnName, String property, ColumnValueHandler valueHandler, XCO xco) throws SQLException {
		String v = getResult(rs, columnName);

		if (null != valueHandler && null != v) {
			Object nv = valueHandler.process(columnName, v);
			if (!(nv instanceof String)) {
				xco.setObjectValue(property, nv);
				return;
			}

			v = (String) nv;

		}

		if (null != v) {
			xco.setStringValue(property, v);
		}
	}
}
