package org.xson.tangyuan.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.xson.common.object.XCO;
import org.xson.tangyuan.mapping.ColumnValueHandler;

public class ByteTypeHandler extends BaseTypeHandler<Byte> {

	public final static ByteTypeHandler instance = new ByteTypeHandler();

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Byte parameter, JdbcType jdbcType) throws SQLException {
		ps.setByte(i, parameter);
	}

	@Override
	public Byte getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return rs.getByte(columnName);
	}

	@Override
	public Byte getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return rs.getByte(columnIndex);
	}

	@Override
	public Byte getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return cs.getByte(columnIndex);
	}

	@Override
	public void appendLog(StringBuilder builder, Byte parameter, DatabaseDialect dialect) {
		// if (DatabaseDialect.MYSQL == dialect) {
		// builder.append(parameter);
		// }
		builder.append(parameter);
	}

	//	@Override
	//	public void setResultToXCO(ResultSet rs, String columnName, String property, XCO xco) throws SQLException {
	//		Byte v = getResult(rs, columnName);
	//		if (null != v) {
	//			xco.setByteValue(property, v.byteValue());
	//		}
	//	}

	@Override
	public void setResultToXCO(ResultSet rs, String columnName, String property, ColumnValueHandler valueHandler, XCO xco) throws SQLException {
		Byte v = getResult(rs, columnName);

		if (null != valueHandler && null != v) {
			Object nv = valueHandler.process(columnName, v);
			if (!(nv instanceof Byte)) {
				xco.setObjectValue(property, nv);
				return;
			}

			v = (Byte) nv;

		}

		if (null != v) {
			xco.setByteValue(property, v.byteValue());
		}
	}

}
