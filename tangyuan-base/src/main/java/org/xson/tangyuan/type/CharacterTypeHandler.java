package org.xson.tangyuan.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.xson.common.object.XCO;

public class CharacterTypeHandler extends BaseTypeHandler<Character> {

	public final static CharacterTypeHandler instance = new CharacterTypeHandler();

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Character parameter, JdbcType jdbcType) throws SQLException {
		ps.setString(i, parameter.toString());
	}

	@Override
	public Character getNullableResult(ResultSet rs, String columnName) throws SQLException {
		String columnValue = rs.getString(columnName);
		if (columnValue != null) {
			return columnValue.charAt(0);
		} else {
			return null;
		}
	}

	@Override
	public Character getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		String columnValue = rs.getString(columnIndex);
		if (columnValue != null) {
			return columnValue.charAt(0);
		} else {
			return null;
		}
	}

	@Override
	public Character getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		String columnValue = cs.getString(columnIndex);
		if (columnValue != null) {
			return columnValue.charAt(0);
		} else {
			return null;
		}
	}

	@Override
	public void appendLog(StringBuilder builder, Character parameter, DatabaseDialect dialect) {
		// if (DatabaseDialect.MYSQL == dialect) {
		// builder.append('\'');
		// builder.append(parameter);
		// builder.append('\'');
		// }
		builder.append('\'');
		builder.append(parameter);
		builder.append('\'');
	}

	@Override
	public void setResultToXCO(ResultSet rs, String columnName, String property, XCO xco) throws SQLException {
		xco.setCharValue(property, getResult(rs, columnName));
	}

}
