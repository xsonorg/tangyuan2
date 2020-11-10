package org.xson.tangyuan.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.xson.common.object.XCO;
import org.xson.tangyuan.mapping.ColumnValueHandler;

public class FloatTypeHandler extends BaseTypeHandler<Float> {

	public final static FloatTypeHandler instance = new FloatTypeHandler();

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Float parameter, JdbcType jdbcType) throws SQLException {
		ps.setFloat(i, parameter);
	}

	@Override
	public Float getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return rs.getFloat(columnName);
	}

	@Override
	public Float getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return rs.getFloat(columnIndex);
	}

	@Override
	public Float getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return cs.getFloat(columnIndex);
	}

	@Override
	public void appendLog(StringBuilder builder, Float parameter, DatabaseDialect dialect) {
		// if (DatabaseDialect.MYSQL == dialect) {
		// builder.append(parameter);
		// }
		builder.append(parameter);
	}

	//	@Override
	//	public void setResultToXCO(ResultSet rs, String columnName, String property, XCO xco) throws SQLException {
	//		Float v = getResult(rs, columnName);
	//		if (null != v) {
	//			xco.setFloatValue(property, v.floatValue());
	//		}
	//	}

	@Override
	public void setResultToXCO(ResultSet rs, String columnName, String property, ColumnValueHandler valueHandler, XCO xco) throws SQLException {
		Float v = getResult(rs, columnName);
		//		if (null != v && null != valueHandler) {
		//			v = (Float) valueHandler.process(columnName, v);
		//		}

		if (null != valueHandler && null != v) {
			Object nv = valueHandler.process(columnName, v);
			if (!(nv instanceof Float)) {
				xco.setObjectValue(property, nv);
				return;
			}

			v = (Float) nv;

		}

		if (null != v) {
			xco.setFloatValue(property, v.floatValue());
		}
	}
}
