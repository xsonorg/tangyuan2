package org.xson.tangyuan.type;

import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import org.xson.common.object.XCO;
import org.xson.tangyuan.mapping.ColumnValueHandler;

public class SqlDateTypeHandler extends BaseTypeHandler<Date> {

	public final static SqlDateTypeHandler instance = new SqlDateTypeHandler();

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Date parameter, JdbcType jdbcType) throws SQLException {
		ps.setDate(i, parameter);
	}

	@Override
	public Date getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return rs.getDate(columnName);
	}

	@Override
	public Date getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return rs.getDate(columnIndex);
	}

	@Override
	public Date getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return cs.getDate(columnIndex);
	}

	@Override
	public void appendLog(StringBuilder builder, Date parameter, DatabaseDialect dialect) {
		// if (DatabaseDialect.MYSQL == dialect) {
		// builder.append('\'');
		// builder.append((null != parameter) ? new SimpleDateFormat("yyyy-MM-dd").format(parameter) : null);
		// builder.append('\'');
		// }
		builder.append('\'');
		builder.append((null != parameter) ? new SimpleDateFormat("yyyy-MM-dd").format(parameter) : null);
		builder.append('\'');
	}

	//	@Override
	//	public void setResultToXCO(ResultSet rs, String columnName, String property, XCO xco) throws SQLException {
	//		java.sql.Date v = getResult(rs, columnName);
	//		if (null != v) {
	//			xco.setDateValue(property, v);
	//		}
	//	}

	@Override
	public void setResultToXCO(ResultSet rs, String columnName, String property, ColumnValueHandler valueHandler, XCO xco) throws SQLException {
		java.sql.Date v = getResult(rs, columnName);

		if (null != valueHandler && null != v) {
			Object nv = valueHandler.process(columnName, v);
			if (!(nv instanceof java.sql.Date)) {
				xco.setObjectValue(property, nv);
				return;
			}

			v = (java.sql.Date) nv;
		}

		if (null != v) {
			xco.setDateValue(property, v);
		}
	}
}
