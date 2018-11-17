package org.xson.tangyuan.mq.executor.activemq;

import javax.jms.JMSException;
import javax.jms.Session;

import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.mq.datasource.activemq.ActiveMqSource;
import org.xson.tangyuan.mq.executor.MqTransactionObject;

public class ActiveMqTransactionObject implements MqTransactionObject {

	private static Log		log		= LogFactory.getLog(ActiveMqTransactionObject.class);

	private Session			session;

	private ActiveMqSource	mqSource;

	private State			state	= State.INITIAL;

	public ActiveMqTransactionObject(ActiveMqSource mqSource, Session session) {
		this.mqSource = mqSource;
		this.session = session;
	}

	@Override
	public void commit() throws Throwable {
		session.commit();
		state = State.COMMIT;
	}

	@Override
	public void rollback() {
		try {
			session.rollback();
		} catch (JMSException e) {
			log.error("session rollback error.", e);
		}
		state = State.ROLLBACK;
	}

	@Override
	public void close() {
		mqSource.closeSession(session, true);
	}

	@Override
	public State getState() {
		return state;
	}

}
