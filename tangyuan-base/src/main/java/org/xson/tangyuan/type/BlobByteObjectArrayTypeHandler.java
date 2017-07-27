package org.xson.tangyuan.type;

import java.io.ByteArrayInputStream;
import java.sql.*;

import org.xson.common.object.XCO;

public class BlobByteObjectArrayTypeHandler extends BaseTypeHandler<Byte[]> {

	public final static BlobByteObjectArrayTypeHandler instance = new BlobByteObjectArrayTypeHandler();

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Byte[] parameter, JdbcType jdbcType) throws SQLException {
		ByteArrayInputStream bis = new ByteArrayInputStream(ByteArrayUtils.convertToPrimitiveArray(parameter));
		ps.setBinaryStream(i, bis, parameter.length);
	}

	@Override
	public Byte[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
		Blob blob = rs.getBlob(columnName);
		return getBytes(blob);
	}

	@Override
	public Byte[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		Blob blob = rs.getBlob(columnIndex);
		return getBytes(blob);
	}

	@Override
	public Byte[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		Blob blob = cs.getBlob(columnIndex);
		return getBytes(blob);
	}

	private Byte[] getBytes(Blob blob) throws SQLException {
		Byte[] returnValue = null;
		if (blob != null) {
			returnValue = ByteArrayUtils.convertToObjectArray(blob.getBytes(1, (int) blob.length()));
		}
		return returnValue;
	}

	@Override
	public void appendLog(StringBuilder builder, Byte[] parameter, DatabaseDialect dialect) {
		// if (DatabaseDialect.MYSQL == dialect) {
		// builder.append(parameter);
		// }
		builder.append(parameter);
	}

	@Override
	public void setResultToXCO(ResultSet rs, String columnName, String property, XCO xco) throws SQLException {
		Blob blob = rs.getBlob(columnName);
		if (blob != null && !rs.wasNull()) {
			byte[] v = blob.getBytes(1, (int) blob.length());
			if (null != v) {
				xco.setByteArrayValue(property, v);
			}
		}
	}

}
