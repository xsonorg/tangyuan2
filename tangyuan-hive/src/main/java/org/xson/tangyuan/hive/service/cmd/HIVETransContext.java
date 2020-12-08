package org.xson.tangyuan.hive.service.cmd;

/**
 * HIVE事物上下文
 */
@FunctionalInterface
public interface HIVETransContext {
	Object execute(HIVECommand cmd) throws Throwable;
}
