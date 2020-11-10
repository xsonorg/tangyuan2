package org.xson.tangyuan.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.xson.common.object.XCO;
import org.xson.tangyuan.mapping.ColumnValueHandler;

public class IntegerTypeHandler extends BaseTypeHandler<Integer> {

	public final static IntegerTypeHandler instance = new IntegerTypeHandler();

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Integer parameter, JdbcType jdbcType) throws SQLException {
		ps.setInt(i, parameter);
	}

	@Override
	public Integer getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return rs.getInt(columnName);
	}

	@Override
	public Integer getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return rs.getInt(columnIndex);
	}

	@Override
	public Integer getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return cs.getInt(columnIndex);
	}

	@Override
	public void appendLog(StringBuilder builder, Integer parameter, DatabaseDialect dialect) {
		// if (DatabaseDialect.MYSQL == dialect) {
		// builder.append(parameter);
		// }
		builder.append(parameter);
	}

	//	@Override
	//	public void setResultToXCO(ResultSet rs, String columnName, String property, XCO xco) throws SQLException {
	//		Integer v = getResult(rs, columnName);
	//		if (null != v) {
	//			xco.setIntegerValue(property, v.intValue());
	//		}
	//	}

	@Override
	public void setResultToXCO(ResultSet rs, String columnName, String property, ColumnValueHandler valueHandler, XCO xco) throws SQLException {
		Integer v = getResult(rs, columnName);

		if (null != valueHandler && null != v) {
			Object nv = valueHandler.process(columnName, v);
			if (!(nv instanceof Integer)) {
				xco.setObjectValue(property, nv);
				return;
			}

			v = (Integer) nv;

		}

		if (null != v) {
			xco.setIntegerValue(property, v.intValue());
		}
	}
}
