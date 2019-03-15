package org.xson.tangyuan.hive.executor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.xson.common.object.XCO;
import org.xson.tangyuan.hive.util.HiveJDBCUtil;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.type.JdbcType;
import org.xson.tangyuan.type.Null;
import org.xson.tangyuan.type.TypeHandler;
import org.xson.tangyuan.type.TypeHandlerRegistry;

public class SqlActuator {

	private TypeHandlerRegistry typeHandlerRegistry;

	public SqlActuator(TypeHandlerRegistry typeHandlerRegistry) {
		this.typeHandlerRegistry = typeHandlerRegistry;
	}

	/**
	 * 结果中忽略表名
	 */
	private boolean ignoreTableName = true; // TODO

	public List<Map<String, Object>> selectAllMap(Connection connection, String sql, List<Object> args, MappingVo resultMap, Integer fetchSize)
			throws SQLException {
		PreparedStatement ps = connection.prepareStatement(sql);
		try {
			setParameters(ps, args);
			if (null != fetchSize && fetchSize.intValue() != ps.getFetchSize()) {
				ps.setFetchSize(fetchSize.intValue());
			}
			ResultSet rs = ps.executeQuery();
			return getResults(rs, resultMap);
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				// ignore
			}
		}
	}

	public List<XCO> selectAllXCO(Connection connection, String sql, List<Object> args, MappingVo resultMap, Integer fetchSize) throws SQLException {
		PreparedStatement ps = connection.prepareStatement(sql);
		try {
			setParameters(ps, args);
			if (null != fetchSize && fetchSize.intValue() != ps.getFetchSize()) {
				ps.setFetchSize(fetchSize.intValue());
			}
			ResultSet rs = ps.executeQuery();
			return getXCOResults(rs, resultMap);
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				// ignore
			}
		}
	}

	public Map<String, Object> selectOneMap(Connection connection, String sql, List<Object> args, MappingVo resultMap, Integer fetchSize)
			throws SQLException {
		List<Map<String, Object>> results = selectAllMap(connection, sql, args, resultMap, fetchSize);

		if (results.size() == 1) {
			return results.get(0);
		}

		if (results.size() > 1) {
			throw new SQLException("Statement returned " + results.size() + " results where exactly one (1) was expected in selectOneMap.");
		}
		return null;
	}

	public XCO selectOneXCO(Connection connection, String sql, List<Object> args, MappingVo resultMap, Integer fetchSize) throws SQLException {
		List<XCO> results = selectAllXCO(connection, sql, args, resultMap, fetchSize);

		if (results.size() == 1) {
			return results.get(0);
		}

		if (results.size() > 1) {
			throw new SQLException("Statement returned " + results.size() + " results where exactly one (1) was expected in selectOneXCO.");
		}
		return null;
	}

	public List<Map<String, Object>> selectAll(Connection connection, String sql, List<Object> args) throws SQLException {
		PreparedStatement ps = connection.prepareStatement(sql);
		try {
			setParameters(ps, args);
			ResultSet rs = ps.executeQuery();
			return getResults(rs, null);
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				// ignore
			}
		}
	}

	public Map<String, Object> selectOne(Connection connection, String sql, List<Object> args) throws SQLException {
		List<Map<String, Object>> results = selectAll(connection, sql, args);

		if (results.size() == 1) {
			return results.get(0);
		}

		if (results.size() > 1) {
			throw new SQLException("Statement returned " + results.size() + " results where exactly one (1) was expected in selectOne.");
		}
		return null;
	}

	public Object selectVar(Connection connection, String sql, List<Object> args) throws SQLException {
		Map<String, Object> results = selectOne(connection, sql, args);
		if (null == results) {
			return null;
		}
		Set<Entry<String, Object>> entries = results.entrySet();
		for (Entry<String, Object> entry : entries) {
			return entry.getValue();
		}
		return null;
	}

	public boolean executeHql(Connection connection, String sql, List<Object> args, boolean async) throws SQLException {
		PreparedStatement ps = connection.prepareStatement(sql);
		try {
			setParameters(ps, args);
			if (async) {
				org.apache.hive.jdbc.HiveStatement stmt = HiveJDBCUtil.getHiveStatementFromProxy(ps);
				return stmt.executeAsync(sql);
			}
			return ps.execute(sql);
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				// ignore
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setParameters(PreparedStatement ps, List<Object> argList) throws SQLException {
		if (null == argList || 0 == argList.size()) {
			return;
		}
		for (int i = 0, n = argList.size(); i < n; i++) {
			Object value = argList.get(i);
			if (value == null) {
				throw new SQLException("SqlActuator requires an instance of Null to represent typed null values for JDBC compatibility");
			} else if (value instanceof Null) {
				((Null) value).getTypeHandler().setParameter(ps, i + 1, null, ((Null) value).getJdbcType());
			} else {
				TypeHandler typeHandler = typeHandlerRegistry.getTypeHandler(value.getClass());
				if (typeHandler == null) {
					throw new SQLException("SqlActuator could not find a TypeHandler instance for " + value.getClass());
				} else {
					typeHandler.setParameter(ps, i + 1, value, null);
				}
			}
		}
	}

	private List<Map<String, Object>> getResults(ResultSet rs, MappingVo resultMap) throws SQLException {
		try {
			List<String> columns = new ArrayList<String>();
			List<TypeHandler<?>> typeHandlers = new ArrayList<TypeHandler<?>>();
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			for (int i = 0; i < columnCount; i++) {
				columns.add(rsmd.getColumnLabel(i + 1));
				try {
					// Class<?> type = Resources.classForName(rsmd.getColumnClassName(i + 1));
					// TypeHandler<?> typeHandler = typeHandlerRegistry.getTypeHandler(type);
					int columnType = rsmd.getColumnType(i + 1);
					JdbcType jdbcType = JdbcType.forCode(columnType);
					TypeHandler<?> typeHandler = typeHandlerRegistry.getTypeHandler(jdbcType);
					if (typeHandler == null) {
						typeHandler = typeHandlerRegistry.getTypeHandler(Object.class);
					}
					typeHandlers.add(typeHandler);
				} catch (Exception e) {
					typeHandlers.add(typeHandlerRegistry.getTypeHandler(Object.class));
				}
			}

			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			if (null == resultMap) {
				while (rs.next()) {
					Map<String, Object> row = new HashMap<String, Object>();
					for (int i = 0, n = columns.size(); i < n; i++) {
						String name = columns.get(i);
						String property = name;
						if (ignoreTableName) {
							property = HiveJDBCUtil.removeTableName(name);
						}
						TypeHandler<?> handler = typeHandlers.get(i);
						// row.put(name.toUpperCase(Locale.ENGLISH), handler.getResult(rs, name));
						row.put(property, handler.getResult(rs, name));
					}
					list.add(row);
				}
			} else {
				while (rs.next()) {
					Map<String, Object> row = new HashMap<String, Object>();
					for (int i = 0, n = columns.size(); i < n; i++) {
						String name = columns.get(i);
						String property = name;
						if (ignoreTableName) {
							property = HiveJDBCUtil.removeTableName(name);
						}
						TypeHandler<?> handler = typeHandlers.get(i);
						row.put(resultMap.getProperty(property), handler.getResult(rs, name));
					}
					list.add(row);
				}
			}
			return list;
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e) {
				// ignore
			}
		}
	}

	private List<XCO> getXCOResults(ResultSet rs, MappingVo resultMap) throws SQLException {
		try {
			List<String> columns = new ArrayList<String>();
			List<TypeHandler<?>> typeHandlers = new ArrayList<TypeHandler<?>>();
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			for (int i = 0; i < columnCount; i++) {
				columns.add(rsmd.getColumnLabel(i + 1));
				try {
					// System.out.println(rsmd.getColumnTypeName(i + 1));
					// rsmd.getColumnType(column)
					// System.out.println();
					// Class<?> type = Resources.classForName(rsmd.getColumnClassName(i + 1));
					// TypeHandler<?> typeHandler = typeHandlerRegistry.getTypeHandler(type);
					int columnType = rsmd.getColumnType(i + 1);
					JdbcType jdbcType = JdbcType.forCode(columnType);
					TypeHandler<?> typeHandler = typeHandlerRegistry.getTypeHandler(jdbcType);
					if (typeHandler == null) {
						typeHandler = typeHandlerRegistry.getTypeHandler(Object.class);
					}
					typeHandlers.add(typeHandler);
				} catch (Exception e) {
					typeHandlers.add(typeHandlerRegistry.getTypeHandler(Object.class));
				}
			}

			List<XCO> list = new ArrayList<XCO>();
			if (null == resultMap) {
				while (rs.next()) {
					XCO row = new XCO();
					for (int i = 0, n = columns.size(); i < n; i++) {
						String name = columns.get(i);
						String property = name;
						if (ignoreTableName) {
							property = HiveJDBCUtil.removeTableName(name);
						}
						TypeHandler<?> handler = typeHandlers.get(i);
						// row.put(name, handler.getResult(rs, name));
						handler.setResultToXCO(rs, name, property, row);
					}
					list.add(row);
				}
			} else {
				while (rs.next()) {
					XCO row = new XCO();
					for (int i = 0, n = columns.size(); i < n; i++) {
						String name = columns.get(i);
						String property = name;
						if (ignoreTableName) {
							property = HiveJDBCUtil.removeTableName(name);
						}
						TypeHandler<?> handler = typeHandlers.get(i);
						handler.setResultToXCO(rs, name, resultMap.getProperty(property), row);
					}
					list.add(row);
				}
			}
			return list;
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e) {
				// ignore
			}
		}
	}
}
