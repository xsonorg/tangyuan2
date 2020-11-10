package org.xson.tangyuan.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.xson.common.object.XCO;
import org.xson.tangyuan.mapping.ColumnValueHandler;

public class ByteArrayTypeHandler extends BaseTypeHandler<byte[]> {

	public final static ByteArrayTypeHandler instance = new ByteArrayTypeHandler();

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, byte[] parameter, JdbcType jdbcType) throws SQLException {
		ps.setBytes(i, parameter);
	}

	@Override
	public byte[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return rs.getBytes(columnName);
	}

	@Override
	public byte[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return rs.getBytes(columnIndex);
	}

	@Override
	public byte[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return cs.getBytes(columnIndex);
	}

	@Override
	public void appendLog(StringBuilder builder, byte[] parameter, DatabaseDialect dialect) {
		// if (DatabaseDialect.MYSQL == dialect) {
		// builder.append(parameter);
		// }
		builder.append(parameter);
	}

	@Override
	public void setResultToXCO(ResultSet rs, String columnName, String property, ColumnValueHandler valueHandler, XCO xco) throws SQLException {
		byte[] v = getResult(rs, columnName);

		if (null != valueHandler && null != v) {
			Object nv = valueHandler.process(columnName, v);
			if (!(nv instanceof byte[])) {
				xco.setObjectValue(property, nv);
				return;
			}

			v = (byte[]) nv;

		}
		if (null != v) {
			xco.setByteArrayValue(property, v);
		}
	}
}
