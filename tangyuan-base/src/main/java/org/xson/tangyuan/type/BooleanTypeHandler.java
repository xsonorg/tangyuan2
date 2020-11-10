package org.xson.tangyuan.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.xson.common.object.XCO;
import org.xson.tangyuan.mapping.ColumnValueHandler;

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

	//	@Override
	//	public void setResultToXCO(ResultSet rs, String columnName, String property, XCO xco) throws SQLException {
	//		Boolean v = getResult(rs, columnName);
	//		if (null != v) {
	//			xco.setBooleanValue(property, v.booleanValue());
	//		}
	//	}

	@Override
	public void setResultToXCO(ResultSet rs, String columnName, String property, ColumnValueHandler valueHandler, XCO xco) throws SQLException {
		Boolean v = getResult(rs, columnName);
		//		if (null != v && null != valueHandler) {
		//			v = (Boolean) valueHandler.process(columnName, v);
		//		}

		if (null != valueHandler && null != v) {
			Object nv = valueHandler.process(columnName, v);
			if (!(nv instanceof Boolean)) {
				xco.setObjectValue(property, nv);
				return;
			}

			v = (Boolean) nv;
		}

		if (null != v) {
			xco.setBooleanValue(property, v.booleanValue());
		}
	}

}
