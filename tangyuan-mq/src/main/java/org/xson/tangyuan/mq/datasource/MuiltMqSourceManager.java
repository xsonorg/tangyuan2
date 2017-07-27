package org.xson.tangyuan.mq.datasource;

import java.util.Map;

public class MuiltMqSourceManager implements MqSourceManager {

	private Map<String, MqSource> mqSourceMap;

	public MuiltMqSourceManager(Map<String, MqSource> mqSourceMap) {
		this.mqSourceMap = mqSourceMap;
	}

	@Override
	public MqSource getMqSource(String msKey) {
		return mqSourceMap.get(msKey);
	}

	@Override
	public void close() {
		for (Map.Entry<String, MqSource> entry : mqSourceMap.entrySet()) {
			entry.getValue().close();
		}
	}

}
