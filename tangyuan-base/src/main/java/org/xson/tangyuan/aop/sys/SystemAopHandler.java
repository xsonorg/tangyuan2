package org.xson.tangyuan.aop.sys;

import java.util.Map;

/**
 * 启动、关闭处理器
 */
public interface SystemAopHandler {

	void execute(Map<String, String> properties);

}
