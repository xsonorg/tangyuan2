package org.xson.tangyuan.type;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.xson.common.object.XCO;

public class BigIntegerTypeHandler extends BaseTypeHandler<BigInteger> {

	public final static BigIntegerTypeHandler instance = new BigIntegerTypeHandler();

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, BigInteger parameter, JdbcType jdbcType) throws SQLException {
		ps.setBigDecimal(i, new BigDecimal(parameter));
	}

	@Override
	public BigInteger getNullableResult(ResultSet rs, String columnName) throws SQLException {
		BigDecimal bigDecimal = rs.getBigDecimal(columnName);
		return bigDecimal == null ? null : bigDecimal.toBigInteger();
	}

	@Override
	public BigInteger getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		BigDecimal bigDecimal = rs.getBigDecimal(columnIndex);
		return bigDecimal == null ? null : bigDecimal.toBigInteger();
	}

	@Override
	public BigInteger getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		BigDecimal bigDecimal = cs.getBigDecimal(columnIndex);
		return bigDecimal == null ? null : bigDecimal.toBigInteger();
	}

	@Override
	public void appendLog(StringBuilder builder, BigInteger parameter, DatabaseDialect dialect) {
		// if (DatabaseDialect.MYSQL == dialect) {
		// builder.append(parameter);
		// }
		builder.append(parameter);
	}

	@Override
	public void setResultToXCO(ResultSet rs, String columnName, String property, XCO xco) throws SQLException {
		BigInteger v = getResult(rs, columnName);
		if (null != v) {
			xco.setBigIntegerValue(property, v);
		}
	}
}
