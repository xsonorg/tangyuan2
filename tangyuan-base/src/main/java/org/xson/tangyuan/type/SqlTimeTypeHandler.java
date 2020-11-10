package org.xson.tangyuan.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.text.SimpleDateFormat;

import org.xson.common.object.XCO;
import org.xson.tangyuan.mapping.ColumnValueHandler;

public class SqlTimeTypeHandler extends BaseTypeHandler<Time> {

	public final static SqlTimeTypeHandler instance = new SqlTimeTypeHandler();

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Time parameter, JdbcType jdbcType) throws SQLException {
		ps.setTime(i, parameter);
	}

	@Override
	public Time getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return rs.getTime(columnName);
	}

	@Override
	public Time getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return rs.getTime(columnIndex);
	}

	@Override
	public Time getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return cs.getTime(columnIndex);
	}

	@Override
	public void appendLog(StringBuilder builder, Time parameter, DatabaseDialect dialect) {
		// if (DatabaseDialect.MYSQL == dialect) {
		// builder.append('\'');
		// builder.append((null != parameter) ? new SimpleDateFormat("HH:mm:ss").format(parameter) : null);
		// builder.append('\'');
		// }
		builder.append('\'');
		builder.append((null != parameter) ? new SimpleDateFormat("HH:mm:ss").format(parameter) : null);
		builder.append('\'');
	}

	@Override
	public void setResultToXCO(ResultSet rs, String columnName, String property, ColumnValueHandler valueHandler, XCO xco) throws SQLException {
		java.sql.Time v = getResult(rs, columnName);

		if (null != valueHandler && null != v) {
			Object nv = valueHandler.process(columnName, v);
			if (!(nv instanceof java.sql.Time)) {
				xco.setObjectValue(property, nv);
				return;
			}

			v = (java.sql.Time) nv;
		}

		if (null != v) {
			xco.setTimeValue(property, v);
		}
	}
}
