package org.xson.tangyuan.hive.util;

import java.sql.PreparedStatement;
import java.sql.Statement;

import org.xson.tangyuan.TangYuanException;

public class HiveJDBCUtil {

	public static org.apache.hive.jdbc.HiveStatement getHiveStatementFromProxy(PreparedStatement ps) {
		Statement stmt = ps;

		if (ps instanceof org.apache.commons.dbcp.DelegatingStatement) {
			// Statement stmt = ((org.apache.commons.dbcp.DelegatingStatement) ps).getDelegate();
			// org.apache.hive.jdbc.HiveStatement
			// org.apache.hive.jdbc.HivePreparedStatement
			while (stmt instanceof org.apache.commons.dbcp.DelegatingStatement) {
				stmt = ((org.apache.commons.dbcp.DelegatingStatement) stmt).getDelegate();
			}
			// if (stmt instanceof org.apache.hive.jdbc.HiveStatement) {
			// return (org.apache.hive.jdbc.HiveStatement) stmt;
			// }
			// if (stmt instanceof org.apache.hive.jdbc.HivePreparedStatement) {
			// return (org.apache.hive.jdbc.HivePreparedStatement) stmt;
			// }
		}

		if (stmt instanceof org.apache.hive.jdbc.HiveStatement) {
			return (org.apache.hive.jdbc.HiveStatement) stmt;
		}
		if (stmt instanceof org.apache.hive.jdbc.HivePreparedStatement) {
			return (org.apache.hive.jdbc.HivePreparedStatement) stmt;
		}

		throw new TangYuanException("Unsupported package type: " + ps.getClass().getName());
	}

	public static String sqlTrim(String sql) {
		if (null == sql) {
			return sql;
		}
		sql = sql.trim();
		if (sql.endsWith(";")) {
			sql = sql.substring(0, sql.length() - 1);
		}
		return sql;
	}

	public static String removeTableName(String name) {
		int pos = name.indexOf(".");
		if (pos > -1) {
			return name.substring(pos + 1);
		}
		return name;
	}
}
