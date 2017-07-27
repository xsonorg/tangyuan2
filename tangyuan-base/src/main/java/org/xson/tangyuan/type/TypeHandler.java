package org.xson.tangyuan.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.xson.common.object.XCO;

public interface TypeHandler<T> {

	void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException;

	T getResult(ResultSet rs, String columnName) throws SQLException;

	T getResult(ResultSet rs, int columnIndex) throws SQLException;

	T getResult(CallableStatement cs, int columnIndex) throws SQLException;

	void appendLog(StringBuilder builder, T parameter, DatabaseDialect dialect);

	void setResultToXCO(ResultSet rs, String columnName, String property, XCO xco) throws SQLException;
}
