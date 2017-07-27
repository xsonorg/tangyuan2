package org.xson.tangyuan.type;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ArrayTypeHandler extends BaseTypeHandler<Object> {

	public final static ArrayTypeHandler instance = new ArrayTypeHandler();

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
		ps.setArray(i, (Array) parameter);
	}

	@Override
	public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
		Array array = rs.getArray(columnName);
		return array == null ? null : array.getArray();
	}

	@Override
	public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		Array array = rs.getArray(columnIndex);
		return array == null ? null : array.getArray();
	}

	@Override
	public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		Array array = cs.getArray(columnIndex);
		return array == null ? null : array.getArray();
	}

	@Override
	public void appendLog(StringBuilder builder, Object parameter, DatabaseDialect dialect) {
		// if (DatabaseDialect.MYSQL == dialect) {
		// // builder.append('\'');
		// builder.append(parameter);
		// // builder.append('\'');
		// }
		// builder.append('\'');
		builder.append(parameter);
		// builder.append('\'');
	}

}
