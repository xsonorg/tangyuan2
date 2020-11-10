package org.xson.tangyuan.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.xson.common.object.XCO;
import org.xson.tangyuan.mapping.ColumnValueHandler;

public class DoubleTypeHandler extends BaseTypeHandler<Double> {

	public final static DoubleTypeHandler instance = new DoubleTypeHandler();

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Double parameter, JdbcType jdbcType) throws SQLException {
		ps.setDouble(i, parameter);
	}

	@Override
	public Double getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return rs.getDouble(columnName);
	}

	@Override
	public Double getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return rs.getDouble(columnIndex);
	}

	@Override
	public Double getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return cs.getDouble(columnIndex);
	}

	@Override
	public void appendLog(StringBuilder builder, Double parameter, DatabaseDialect dialect) {
		// if (DatabaseDialect.MYSQL == dialect) {
		// builder.append(parameter);
		// }
		builder.append(parameter);
	}

	//	@Override
	//	public void setResultToXCO(ResultSet rs, String columnName, String property, XCO xco) throws SQLException {
	//		Double v = getResult(rs, columnName);
	//		if (null != v) {
	//			xco.setDoubleValue(property, v.doubleValue());
	//		}
	//	}

	@Override
	public void setResultToXCO(ResultSet rs, String columnName, String property, ColumnValueHandler valueHandler, XCO xco) throws SQLException {
		Double v = getResult(rs, columnName);
		//		if (null != v && null != valueHandler) {
		//			v = (Double) valueHandler.process(columnName, v);
		//		}

		if (null != valueHandler && null != v) {
			Object nv = valueHandler.process(columnName, v);
			if (!(nv instanceof Double)) {
				xco.setObjectValue(property, nv);
				return;
			}

			v = (Double) nv;

		}

		if (null != v) {
			xco.setDoubleValue(property, v.doubleValue());
		}
	}
}
