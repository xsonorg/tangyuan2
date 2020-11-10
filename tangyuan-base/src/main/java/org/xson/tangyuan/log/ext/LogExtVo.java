package org.xson.tangyuan.log.ext;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class LogExtVo {

	private Map<String, Integer>	excludeTypeMap				= null;
	private boolean					enableContextLog			= false;

	// sql
	private boolean					sqlErrorLogPrint			= false;

	// web
	private boolean					webRequestHeaderPrint		= false;
	private boolean					webResponseResultPrint		= false;

	// es
	private boolean					esResponseResultPrint		= false;
	private boolean					esResponseResultErrorPrint	= false;

	// mongo
	private boolean					mongoSqlPrint				= false;
	private boolean					mongoSqlShellPrint			= false;
	private boolean					mongoErrorLogPrint			= false;

	public LogExtVo(Properties p) throws Throwable {
		init(p);
	}

	private void init(Properties p) throws Throwable {
		if (null == p) {
			return;
		}

		String enable_context_log = getEmpty(p.getProperty("enable_context_log"));
		if (null != enable_context_log) {
			enableContextLog = Boolean.parseBoolean(enable_context_log);
		}

		// sql
		String sql_error_log_print = getEmpty(p.getProperty("sql_error_log_print"));
		if (null != sql_error_log_print) {
			sqlErrorLogPrint = Boolean.parseBoolean(sql_error_log_print);
		}

		// web
		String web_request_header_print = getEmpty(p.getProperty("web_request_header_print"));
		if (null != web_request_header_print) {
			webRequestHeaderPrint = Boolean.parseBoolean(web_request_header_print);
		}
		String web_response_result_print = getEmpty(p.getProperty("web_response_result_print"));
		if (null != web_response_result_print) {
			webResponseResultPrint = Boolean.parseBoolean(web_response_result_print);
		}

		// es
		String es_response_result_print = getEmpty(p.getProperty("es_response_result_print"));
		if (null != es_response_result_print) {
			esResponseResultPrint = Boolean.parseBoolean(es_response_result_print);
		}
		String es_response_result_error_print = getEmpty(p.getProperty("es_response_result_error_print"));
		if (null != es_response_result_error_print) {
			esResponseResultErrorPrint = Boolean.parseBoolean(es_response_result_error_print);
		}

		// mongo
		String mongo_sql_print = getEmpty(p.getProperty("mongo_sql_print"));
		if (null != mongo_sql_print) {
			mongoSqlPrint = Boolean.parseBoolean(mongo_sql_print);
		}
		String mongo_sql_shell_print = getEmpty(p.getProperty("mongo_sql_shell_print"));
		if (null != mongo_sql_shell_print) {
			mongoSqlShellPrint = Boolean.parseBoolean(mongo_sql_shell_print);
		}
		// String mongo_shell_print = getEmpty(p.getProperty("mongo_shell_print"));
		// if (null != mongo_shell_print) {
		// mongoShellPrint = Boolean.parseBoolean(mongo_shell_print);
		// }
		String mongo_error_log_print = getEmpty(p.getProperty("mongo_error_log_print"));
		if (null != mongo_error_log_print) {
			mongoErrorLogPrint = Boolean.parseBoolean(mongo_error_log_print);
		}

		// other

		String exclude_type = getEmpty(p.getProperty("exclude_origin"));
		if (null != exclude_type) {
			excludeTypeMap = new HashMap<String, Integer>();
			fillToMap(exclude_type.split(","), excludeTypeMap);
		}
	}

	private void fillToMap(String[] array, Map<String, Integer> map) {
		for (int i = 0; i < array.length; i++) {
			map.put(array[i].trim().toUpperCase(), 1);
		}
	}

	private String getEmpty(String str) {
		if (null == str) {
			return null;
		}
		str = str.trim();
		if (0 == str.length()) {
			return null;
		}
		return str;
	}

	public boolean isExclude(String origin) {
		if (null == origin) {
			return false;
		}
		// 检查排除的
		if (null != excludeTypeMap && excludeTypeMap.containsKey(origin)) {
			return true;
		}
		return false;
	}

	public boolean isEnableContextLog() {
		return enableContextLog;
	}

	public boolean isSqlErrorLogPrint() {
		return sqlErrorLogPrint;
	}

	public boolean isWebRequestHeaderPrint() {
		return webRequestHeaderPrint;
	}

	public boolean isWebResponseResultPrint() {
		return webResponseResultPrint;
	}

	public boolean isEsResponseResultPrint() {
		return esResponseResultPrint;
	}

	public boolean isEsResponseResultErrorPrint() {
		return esResponseResultErrorPrint;
	}

	public boolean isMongoSqlPrint() {
		return mongoSqlPrint;
	}

	public boolean isMongoSqlShellPrint() {
		return mongoSqlShellPrint;
	}

	// public boolean isMongoShellPrint() {
	// return mongoShellPrint;
	// }

	public boolean isMongoErrorLogPrint() {
		return mongoErrorLogPrint;
	}

}
