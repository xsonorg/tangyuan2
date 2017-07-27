package org.xson.tangyuan.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.xson.common.object.XCO;

public class ByteObjectArrayTypeHandler extends BaseTypeHandler<Byte[]> {

	public final static ByteObjectArrayTypeHandler instance = new ByteObjectArrayTypeHandler();

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Byte[] parameter, JdbcType jdbcType) throws SQLException {
		ps.setBytes(i, ByteArrayUtils.convertToPrimitiveArray(parameter));
	}

	@Override
	public Byte[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
		byte[] bytes = rs.getBytes(columnName);
		return getBytes(bytes);
	}

	@Override
	public Byte[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		byte[] bytes = rs.getBytes(columnIndex);
		return getBytes(bytes);
	}

	@Override
	public Byte[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		byte[] bytes = cs.getBytes(columnIndex);
		return getBytes(bytes);
	}

	private Byte[] getBytes(byte[] bytes) {
		Byte[] returnValue = null;
		if (bytes != null) {
			returnValue = ByteArrayUtils.convertToObjectArray(bytes);
		}
		return returnValue;
	}

	@Override
	public void appendLog(StringBuilder builder, Byte[] parameter, DatabaseDialect dialect) {
		// if (DatabaseDialect.MYSQL == dialect) {
		// // builder.append('\'');
		// builder.append(parameter);
		// // builder.append('\'');
		// }
		// builder.append('\'');
		builder.append(parameter);
		// builder.append('\'');
	}

	@Override
	public void setResultToXCO(ResultSet rs, String columnName, String property, XCO xco) throws SQLException {
		byte[] v = rs.getBytes(columnName);
		if (v != null && !rs.wasNull()) {
			xco.setByteArrayValue(property, v);
		}
	}

}
