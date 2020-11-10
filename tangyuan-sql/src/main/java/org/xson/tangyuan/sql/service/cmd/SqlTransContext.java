package org.xson.tangyuan.sql.service.cmd;

/**
 * SQL事物上下文
 */
@FunctionalInterface
public interface SqlTransContext {
	Object execute(SQLCommand cmd) throws Throwable;
}
