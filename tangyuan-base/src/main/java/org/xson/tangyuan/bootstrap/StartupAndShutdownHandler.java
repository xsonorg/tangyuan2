package org.xson.tangyuan.bootstrap;

import java.util.Map;

/**
 * 启动、关闭处理器
 */
public interface StartupAndShutdownHandler {

	void execute(Map<String, String> properties);

}
