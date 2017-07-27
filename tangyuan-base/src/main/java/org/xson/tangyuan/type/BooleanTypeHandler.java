package org.xson.tangyuan.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.xson.common.object.XCO;

public class BooleanTypeHandler extends BaseTypeHandler<Boolean> {

	public final static BooleanTypeHandler instance = new BooleanTypeHandler();

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Boolean parameter, JdbcType jdbcType) throws SQLException {
		ps.setBoolean(i, parameter);
	}

	@Override
	public Boolean getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return rs.getBoolean(columnName);
	}

	@Override
	public Boolean getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return rs.getBoolean(columnIndex);
	}

	@Override
	public Boolean getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return cs.getBoolean(columnIndex);
	}

	@Override
	public void appendLog(StringBuilder builder, Boolean parameter, DatabaseDialect dialect) {
		// if (DatabaseDialect.MYSQL == dialect) {
		// // MYSQL保存BOOLEAN值时用1代表TRUE,0代表FALSE, boolean在MySQL里的类型为tinyint(1)
		// if (parameter) {
		// builder.append(1);
		// } else {
		// builder.append(0);
		// }
		// }
		if (parameter) {
			builder.append(1);
		} else {
			builder.append(0);
		}
	}

	@Override
	public void setResultToXCO(ResultSet rs, String columnName, String property, XCO xco) throws SQLException {
		Boolean v = getResult(rs, columnName);
		if (null != v) {
			xco.setBooleanValue(property, v.booleanValue());
		}
	}

}
