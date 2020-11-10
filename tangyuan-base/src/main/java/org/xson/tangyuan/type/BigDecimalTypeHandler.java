package org.xson.tangyuan.type;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.xson.common.object.XCO;
import org.xson.tangyuan.mapping.ColumnValueHandler;

public class BigDecimalTypeHandler extends BaseTypeHandler<BigDecimal> {

	public final static BigDecimalTypeHandler instance = new BigDecimalTypeHandler();

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, BigDecimal parameter, JdbcType jdbcType) throws SQLException {
		ps.setBigDecimal(i, parameter);
	}

	@Override
	public BigDecimal getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return rs.getBigDecimal(columnName);
	}

	@Override
	public BigDecimal getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return rs.getBigDecimal(columnIndex);
	}

	@Override
	public BigDecimal getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return cs.getBigDecimal(columnIndex);
	}

	@Override
	public void appendLog(StringBuilder builder, BigDecimal parameter, DatabaseDialect dialect) {
		// if (DatabaseDialect.MYSQL == dialect) {
		// builder.append(parameter);
		// }
		builder.append(parameter);
	}

	@Override
	public void setResultToXCO(ResultSet rs, String columnName, String property, ColumnValueHandler valueHandler, XCO xco) throws SQLException {
		BigDecimal v = getResult(rs, columnName);

		if (null != valueHandler && null != v) {
			Object nv = valueHandler.process(columnName, v);
			if (!(nv instanceof BigDecimal)) {
				xco.setObjectValue(property, nv);
				return;
			}

			v = (BigDecimal) nv;
		}

		if (null != v) {
			xco.setBigDecimalValue(property, v);
		}
	}

}
