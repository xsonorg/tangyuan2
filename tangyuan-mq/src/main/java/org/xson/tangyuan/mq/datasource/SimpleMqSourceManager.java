package org.xson.tangyuan.mq.datasource;

public class SimpleMqSourceManager implements MqSourceManager {

	private MqSource mySource;

	public SimpleMqSourceManager(MqSource mySource) {
		this.mySource = mySource;
	}

	@Override
	public MqSource getMqSource(String msKey) {
		return mySource;
	}

	@Override
	public void close() {
		mySource.close();
	}

}
