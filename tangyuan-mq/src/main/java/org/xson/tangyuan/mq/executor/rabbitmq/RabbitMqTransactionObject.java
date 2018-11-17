package org.xson.tangyuan.mq.executor.rabbitmq;

import java.io.IOException;

import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.mq.datasource.rabbitmq.RabbitMqSource;
import org.xson.tangyuan.mq.executor.MqTransactionObject;

import com.rabbitmq.client.Channel;

public class RabbitMqTransactionObject implements MqTransactionObject {

	private static Log		log		= LogFactory.getLog(RabbitMqTransactionObject.class);

	private RabbitMqSource	mqSource;

	private Channel			channel;

	private State			state	= State.INITIAL;

	public RabbitMqTransactionObject(RabbitMqSource mqSource, Channel channel) {
		this.mqSource = mqSource;
		this.channel = channel;
	}

	@Override
	public void commit() throws Throwable {
		channel.txCommit();
		state = State.COMMIT;
	}

	@Override
	public void rollback() {
		try {
			channel.txRollback();
		} catch (IOException e) {
			log.error("channel rollback error.", e);
		}
		state = State.ROLLBACK;
	}

	@Override
	public void close() {
		// try {
		// channel.close();
		// } catch (Throwable e) {
		// log.error("channel close error.", e);
		// }
		mqSource.closeChannel(channel);
	}

	@Override
	public State getState() {
		return state;
	}

}
