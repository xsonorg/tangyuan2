package org.xson.tangyuan.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.xson.common.object.XCO;
import org.xson.tangyuan.mapping.ColumnValueHandler;

public abstract class BaseTypeHandler<T> extends TypeReference<T> implements TypeHandler<T> {

	public void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
		if (parameter == null) {
			if (jdbcType == null) {
				throw new TypeException("JDBC requires that the JdbcType must be specified for all nullable parameters.");
			}
			try {
				ps.setNull(i, jdbcType.TYPE_CODE);
			} catch (SQLException e) {
				throw new TypeException("Error setting null for parameter #" + i + " with JdbcType " + jdbcType + " . "
						+ "Try setting a different JdbcType for this parameter or a different jdbcTypeForNull configuration property. " + "Cause: " + e, e);
			}
		} else {
			setNonNullParameter(ps, i, parameter, jdbcType);
		}
	}

	public T getResult(ResultSet rs, String columnName) throws SQLException {
		T result = getNullableResult(rs, columnName);
		if (rs.wasNull()) {
			return null;
		} else {
			return result;
		}
	}

	public T getResult(ResultSet rs, int columnIndex) throws SQLException {
		T result = getNullableResult(rs, columnIndex);
		if (rs.wasNull()) {
			return null;
		} else {
			return result;
		}
	}

	public T getResult(CallableStatement cs, int columnIndex) throws SQLException {
		T result = getNullableResult(cs, columnIndex);
		if (cs.wasNull()) {
			return null;
		} else {
			return result;
		}
	}

	public abstract void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException;

	public abstract T getNullableResult(ResultSet rs, String columnName) throws SQLException;

	public abstract T getNullableResult(ResultSet rs, int columnIndex) throws SQLException;

	public abstract T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException;

	protected String replace(String str) {
		if (null != str) {
			// return str.replaceAll("'", "''").replaceAll("\\", "\\\\");
			return str.replaceAll("'", "''");
		}
		return null;
	}

	@Override
	public void setResultToXCO(ResultSet rs, String columnName, String property, ColumnValueHandler valueHandler, XCO xco) throws SQLException {
		throw new SQLException("Sub class needs to be implemented");
	}

	//	@Override
	//	public void setResultToXCO(ResultSet rs, String columnName, String property, XCO xco) throws SQLException {
	//		throw new SQLException("Sub class needs to be implemented");
	//	}
}
