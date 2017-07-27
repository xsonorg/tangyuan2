package org.xson.tangyuan.mq.datasource;

import java.util.HashMap;
import java.util.Map;

import org.xson.tangyuan.mq.datasource.MqSourceVo.MqSourceType;
import org.xson.tangyuan.mq.datasource.activemq.ActiveMqSource;
import org.xson.tangyuan.mq.datasource.rabbitmq.RabbitMqSource;

public class MqManagerCreater {

	public MqSourceManager create(String defaultMqSource, Map<String, MqSourceVo> mqSourceVoMap) throws Throwable {
		if (1 == mqSourceVoMap.size()) {
			return new SimpleMqSourceManager(createMqSource(mqSourceVoMap.get(defaultMqSource)));
		}
		return createMuilt(defaultMqSource, mqSourceVoMap);
	}

	private MqSource createMqSource(MqSourceVo msVo) throws Throwable {
		MqSource mqSource = null;
		if (MqSourceType.ActiveMQ == msVo.getType()) {
			mqSource = new ActiveMqSource();
			((ActiveMqSource) mqSource).init(msVo);
		} else if (MqSourceType.RabbitMQ == msVo.getType()) {
			mqSource = new RabbitMqSource();
			((RabbitMqSource) mqSource).init(msVo);
		}
		return mqSource;
	}

	private MqSourceManager createMuilt(String defaultMqSource, Map<String, MqSourceVo> mqSourceVoMap) throws Throwable {
		Map<String, MqSource> mqSourceMap = new HashMap<String, MqSource>();
		for (Map.Entry<String, MqSourceVo> entry : mqSourceVoMap.entrySet()) {
			mqSourceMap.put(entry.getKey(), createMqSource(entry.getValue()));
		}
		return new MuiltMqSourceManager(mqSourceMap);
	}
}
