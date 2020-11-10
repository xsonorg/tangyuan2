package org.xson.tangyuan.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.xson.common.object.XCO;
import org.xson.tangyuan.mapping.ColumnValueHandler;

public class SqlTimestampTypeHandler extends BaseTypeHandler<Timestamp> {

	public final static SqlTimestampTypeHandler instance = new SqlTimestampTypeHandler();

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Timestamp parameter, JdbcType jdbcType) throws SQLException {
		ps.setTimestamp(i, parameter);
	}

	@Override
	public Timestamp getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return rs.getTimestamp(columnName);
	}

	@Override
	public Timestamp getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return rs.getTimestamp(columnIndex);
	}

	@Override
	public Timestamp getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return cs.getTimestamp(columnIndex);
	}

	@Override
	public void appendLog(StringBuilder builder, Timestamp parameter, DatabaseDialect dialect) {
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
	//		java.sql.Timestamp v = getResult(rs, columnName);
	//		if (null != v) {
	//			xco.setTimestampValue(property, v);
	//		}
	//	}

	@Override
	public void setResultToXCO(ResultSet rs, String columnName, String property, ColumnValueHandler valueHandler, XCO xco) throws SQLException {
		java.sql.Timestamp v = getResult(rs, columnName);
		//		if (null != valueHandler && null != v) {
		//			v = (java.sql.Timestamp) valueHandler.process(columnName, v);
		//		}

		if (null != valueHandler && null != v) {
			Object nv = valueHandler.process(columnName, v);
			if (!(nv instanceof java.sql.Timestamp)) {
				xco.setObjectValue(property, nv);
				return;
			}
		}

		if (null != v) {
			xco.setTimestampValue(property, v);
		}
	}
}
