package org.xson.tangyuan.sql.util;

import java.util.ArrayList;
import java.util.List;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.type.DatabaseDialect;
import org.xson.tangyuan.type.TypeHandler;
import org.xson.tangyuan.type.TypeHandlerRegistry;

public class SqlLog {

	private static Log			log			= LogFactory.getLog(SqlLog.class);

	private TypeHandlerRegistry	registry	= null;

	public SqlLog(TypeHandlerRegistry registry) {
		this.registry = registry;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String getSqlLog(String sql, List<Object> argList) {
		if (null == argList || 0 == argList.size()) {
			return sql;
		} else {
			List<Object> list = parse(sql, argList.size());
			if (null == list) {
				return sql;
			} else {
				StringBuilder builder = new StringBuilder();
				int index = 0;
				for (Object obj : list) {
					if (null == obj) {
						Object var = argList.get(index);
						if (null != var) {
							TypeHandler typeHandler = registry.getTypeHandler(var.getClass());
							if (null != typeHandler) {
								// typeHandler.appendLog(builder, var, DatabaseDialect.MYSQL);
								typeHandler.appendLog(builder, var, DatabaseDialect.NULL);
							} else {
								log.info("不能解析的参数: " + var);
							}
						} else {
							builder.append(var);
						}
						index++;
					} else {
						builder.append(obj);
					}
				}
				return builder.toString();
			}
		}
	}

	private List<Object> parse(String sql, int argSize) {
		int count = 0;
		List<Object> list = new ArrayList<>();
		char[] src = sql.toCharArray();
		StringBuilder builder = new StringBuilder();
		boolean inString = false;
		for (int i = 0; i < src.length; i++) {
			char key = src[i];
			switch (key) {
			case '\'':
				if (inString) {
					inString = false;
				} else {
					inString = true;
				}
				builder.append(key);
				break;
			case '?':
				if (inString) {
					builder.append(key);
				} else {
					list.add(builder.toString());
					builder = new StringBuilder();
					list.add(null);// replace
					count++;
				}
				break;
			default:
				builder.append(key);
			}
		}

		if (builder.length() > 0) {
			list.add(builder.toString());
		}

		if (count > 0) {
			if (count > argSize) {
				throw new TangYuanException("所需SQL变量数和入参数不符");
			}
			return list;
		}
		return null;
	}
}
