package org.xson.tangyuan.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.xson.common.object.XCO;
import org.xson.tangyuan.mapping.ColumnValueHandler;

public class TimeOnlyTypeHandler extends BaseTypeHandler<Date> {

	public final static TimeOnlyTypeHandler instance = new TimeOnlyTypeHandler();

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Date parameter, JdbcType jdbcType) throws SQLException {
		ps.setTime(i, new Time(parameter.getTime()));
	}

	@Override
	public Date getNullableResult(ResultSet rs, String columnName) throws SQLException {
		java.sql.Time sqlTime = rs.getTime(columnName);
		if (sqlTime != null) {
			return new Date(sqlTime.getTime());
		}
		return null;
	}

	@Override
	public Date getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		java.sql.Time sqlTime = rs.getTime(columnIndex);
		if (sqlTime != null) {
			return new Date(sqlTime.getTime());
		}
		return null;
	}

	@Override
	public Date getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		java.sql.Time sqlTime = cs.getTime(columnIndex);
		if (sqlTime != null) {
			return new Date(sqlTime.getTime());
		}
		return null;
	}

	@Override
	public void appendLog(StringBuilder builder, Date parameter, DatabaseDialect dialect) {
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
		java.util.Date v = getResult(rs, columnName);
		if (null != valueHandler && null != v) {
			Object nv = valueHandler.process(columnName, v);
			if (!(nv instanceof java.util.Date)) {
				xco.setObjectValue(property, nv);
				return;
			}

			v = (java.util.Date) nv;
		}
		if (null != v) {
			xco.setTimeValue(property, new java.sql.Time(v.getTime()));
		}
	}
}
