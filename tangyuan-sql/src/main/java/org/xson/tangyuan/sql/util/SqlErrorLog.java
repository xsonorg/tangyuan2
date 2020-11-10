package org.xson.tangyuan.sql.util;

import java.util.ArrayList;
import java.util.List;

import org.xson.tangyuan.type.DatabaseDialect;
import org.xson.tangyuan.type.TypeHandler;
import org.xson.tangyuan.type.TypeHandlerRegistry;
import org.xson.tangyuan.util.TangYuanUtil;

public class SqlErrorLog {

	private TypeHandlerRegistry registry = null;

	public SqlErrorLog(TypeHandlerRegistry registry) {
		this.registry = registry;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String getSqlLog(String sql, List<Object> argList, StringBuilder errorBuilder) {
		if (null == argList || 0 == argList.size()) {
			return sql;
		} else {
			List<Object> list = parse(sql, argList.size(), errorBuilder);
			if (null == list) {
				return sql;
			} else {
				StringBuilder builder = new StringBuilder();
				int index = 0;
				for (Object obj : list) {
					if (null == obj) {
						// Object var = argList.get(index);

						Object var = null;
						if (index < argList.size()) {
							var = argList.get(index);
						} else {
							var = "**缺少参数**";
						}

						if (null != var) {
							TypeHandler typeHandler = registry.getTypeHandler(var.getClass());
							if (null != typeHandler) {
								typeHandler.appendLog(builder, var, DatabaseDialect.NULL);
							} else {
								// log.info("不能解析的参数: " + var);
								builder.append(var);
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

	private List<Object> parse(String sql, int argSize, StringBuilder errorBuilder) {
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
				// throw new TangYuanException("所需SQL变量数和入参数不符");
				errorBuilder.append(TangYuanUtil.format("所需SQL变量数量[{}]和入参数量[{}]不符", count, argSize));
			}
			return list;
		}
		return null;
	}
}
