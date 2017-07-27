package org.xson.tangyuan.mq.datasource;

public interface MqSourceManager {

	public MqSource getMqSource(String msKey);

	public void close();

}
