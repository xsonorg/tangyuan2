package org.xson.tangyuan.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.xson.common.object.XCO;
import org.xson.tangyuan.mapping.ColumnValueHandler;

public class ShortTypeHandler extends BaseTypeHandler<Short> {

	public final static ShortTypeHandler instance = new ShortTypeHandler();

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Short parameter, JdbcType jdbcType) throws SQLException {
		ps.setShort(i, parameter);
	}

	@Override
	public Short getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return rs.getShort(columnName);
	}

	@Override
	public Short getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return rs.getShort(columnIndex);
	}

	@Override
	public Short getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return cs.getShort(columnIndex);
	}

	@Override
	public void appendLog(StringBuilder builder, Short parameter, DatabaseDialect dialect) {
		// if (DatabaseDialect.MYSQL == dialect) {
		// builder.append(parameter);
		// }
		builder.append(parameter);
	}

	//	@Override
	//	public void setResultToXCO(ResultSet rs, String columnName, String property, XCO xco) throws SQLException {
	//		Short v = getResult(rs, columnName);
	//		if (null != v) {
	//			xco.setShortValue(property, v.shortValue());
	//		}
	//	}

	@Override
	public void setResultToXCO(ResultSet rs, String columnName, String property, ColumnValueHandler valueHandler, XCO xco) throws SQLException {
		Short v = getResult(rs, columnName);

		if (null != valueHandler && null != v) {
			Object nv = valueHandler.process(columnName, v);
			if (!(nv instanceof Short)) {
				xco.setObjectValue(property, nv);
				return;
			}

			v = (Short) nv;
		}

		if (null != v) {
			xco.setShortValue(property, v.shortValue());
		}
	}

}
