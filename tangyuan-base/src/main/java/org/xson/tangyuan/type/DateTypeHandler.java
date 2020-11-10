package org.xson.tangyuan.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.xson.common.object.XCO;
import org.xson.tangyuan.mapping.ColumnValueHandler;

public class DateTypeHandler extends BaseTypeHandler<Date> {

	public final static DateTypeHandler instance = new DateTypeHandler();

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Date parameter, JdbcType jdbcType) throws SQLException {
		ps.setTimestamp(i, new Timestamp((parameter).getTime()));
	}

	@Override
	public Date getNullableResult(ResultSet rs, String columnName) throws SQLException {
		Timestamp sqlTimestamp = rs.getTimestamp(columnName);
		if (sqlTimestamp != null) {
			return new Date(sqlTimestamp.getTime());
		}
		return null;
	}

	@Override
	public Date getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		Timestamp sqlTimestamp = rs.getTimestamp(columnIndex);
		if (sqlTimestamp != null) {
			return new Date(sqlTimestamp.getTime());
		}
		return null;
	}

	@Override
	public Date getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		Timestamp sqlTimestamp = cs.getTimestamp(columnIndex);
		if (sqlTimestamp != null) {
			return new Date(sqlTimestamp.getTime());
		}
		return null;
	}

	@Override
	public void appendLog(StringBuilder builder, Date parameter, DatabaseDialect dialect) {
		// if (DatabaseDialect.MYSQL == dialect) {
		// builder.append('\'');
		// builder.append((null != parameter) ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(parameter) : null);
		// builder.append('\'');
		// }
		builder.append('\'');
		builder.append((null != parameter) ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(parameter) : null);
		builder.append('\'');
	}

	//	@Override
	//	public void setResultToXCO(ResultSet rs, String columnName, String property, XCO xco) throws SQLException {
	//		java.util.Date v = getResult(rs, columnName);
	//		if (null != v) {
	//			xco.setDateTimeValue(property, v);
	//		}
	//	}

	@Override
	public void setResultToXCO(ResultSet rs, String columnName, String property, ColumnValueHandler valueHandler, XCO xco) throws SQLException {
		java.util.Date v = getResult(rs, columnName);
		//		if (null != v && null != valueHandler) {
		//			v = (java.util.Date) valueHandler.process(columnName, v);
		//		}

		if (null != valueHandler && null != v) {
			Object nv = valueHandler.process(columnName, v);
			if (!(nv instanceof java.util.Date)) {
				xco.setObjectValue(property, nv);
				return;
			}

			v = (java.util.Date) nv;

		}

		if (null != v) {
			xco.setDateTimeValue(property, v);
		}
	}

}
