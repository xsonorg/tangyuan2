package org.xson.tangyuan.mq.executor.activemq;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.mq.MqComponent;
import org.xson.tangyuan.mq.datasource.activemq.ActiveMqSource;
import org.xson.tangyuan.mq.executor.Sender;
import org.xson.tangyuan.mq.service.context.MqServiceContext;
import org.xson.tangyuan.mq.vo.ActiveMqChannelVo;
import org.xson.tangyuan.mq.vo.ChannelVo;
import org.xson.tangyuan.mq.vo.ChannelVo.ChannelType;
import org.xson.tangyuan.mq.vo.RoutingVo;

public class ActiveMqSender extends Sender {

	private Log log = LogFactory.getLog(getClass());

	@Override
	public void sendMessage(ChannelVo queue, RoutingVo rVo, Object arg, boolean useTx, MqServiceContext context) throws Throwable {

		if (!isRouting(rVo, arg)) {
			log.info("The current queue[" + queue.getName() + "] does not match the routing rule and does not send the message.");
			return;
		}

		if (ChannelType.Queue == queue.getType()) {
			sendQueueMessage((ActiveMqChannelVo) queue, arg, useTx, context);
		} else if (ChannelType.Topic == queue.getType()) {
			sendTopicMessage((ActiveMqChannelVo) queue, arg, useTx, context);
		} else {
			throw new TangYuanException("Unsupported queue type: " + queue.getType().toString());
		}

	}

	private void sendQueueMessage(ActiveMqChannelVo queue, Object arg, boolean useTx, MqServiceContext context) throws Throwable {
		ActiveMqSource mqSource        = (ActiveMqSource) MqComponent.getInstance().getMqSourceManager().getMqSource(queue.getMsKey());
		Connection     connection      = null;
		Session        session         = null;
		Throwable      tx              = null;

		boolean        transacted      = false;
		int            acknowledgeMode = Session.AUTO_ACKNOWLEDGE;
		if (useTx) {
			transacted = true;
			acknowledgeMode = Session.SESSION_TRANSACTED;
		}

		try {
			session = mqSource.getSession(transacted, acknowledgeMode);
			if (null == session) {
				connection = mqSource.getConnection();
				session = connection.createSession(transacted, acknowledgeMode);
			}

			if (useTx) {
				context.addTransactionObject(new ActiveMqTransactionObject(mqSource, session));
			}

			Destination     destination     = session.createQueue(queue.getName());
			MessageProducer messageProducer = session.createProducer(destination);
			TextMessage     message         = session.createTextMessage(((XCO) arg).toXMLString());

			// int deliveryMode = (Integer) queue.getProperties().get(ActiveMqVo.ACTIVEMQ_P_DELIVERYMODE);
			// long timeToLive = (Long) queue.getProperties().get(ActiveMqVo.ACTIVEMQ_P_TIMETOLIVE);

			int             deliveryMode    = queue.getDeliveryMode();
			long            timeToLive      = queue.getTimeToLive();

			messageProducer.send(message, deliveryMode, Message.DEFAULT_PRIORITY, timeToLive);

			// log.info("send queue: " + ((XCO) arg).toXMLString());
			log.info("send message to queue[" + queue.getName() + "]: " + ((XCO) arg).toXMLString());

		} catch (Throwable e) {
			tx = e;
		} finally {
			if (null != session && !useTx) {
				mqSource.closeSession(session, transacted);
			}
			if (null != tx) {
				throw tx;
			}
		}
	}

	private void sendTopicMessage(ActiveMqChannelVo queue, Object arg, boolean useTx, MqServiceContext context) throws Throwable {
		ActiveMqSource mqSource        = (ActiveMqSource) MqComponent.getInstance().getMqSourceManager().getMqSource(queue.getMsKey());
		Connection     connection      = null;
		Session        session         = null;
		Throwable      tx              = null;

		boolean        transacted      = false;
		int            acknowledgeMode = Session.AUTO_ACKNOWLEDGE;
		if (useTx) {
			transacted = true;
			acknowledgeMode = Session.SESSION_TRANSACTED;
		}

		try {

			session = mqSource.getSession(transacted, acknowledgeMode);
			if (null == session) {
				connection = mqSource.getConnection();
				session = connection.createSession(transacted, acknowledgeMode);
			}

			if (useTx) {
				context.addTransactionObject(new ActiveMqTransactionObject(mqSource, session));
			}

			Destination     destination     = session.createTopic(queue.getName());
			MessageProducer messageProducer = session.createProducer(destination);
			TextMessage     message         = session.createTextMessage(((XCO) arg).toXMLString());

			// int deliveryMode = (Integer) queue.getProperties().get(ActiveMqVo.ACTIVEMQ_P_DELIVERYMODE);
			// long timeToLive = (Long) queue.getProperties().get(ActiveMqVo.ACTIVEMQ_P_TIMETOLIVE);

			int             deliveryMode    = queue.getDeliveryMode();
			long            timeToLive      = queue.getTimeToLive();

			messageProducer.send(message, deliveryMode, Message.DEFAULT_PRIORITY, timeToLive);

			log.info("send message to topic[" + queue.getName() + "]: " + ((XCO) arg).toXMLString());

		} catch (Throwable e) {
			tx = e;
		} finally {
			if (null != session && !useTx) {
				mqSource.closeSession(session, transacted);
			}
			if (null != tx) {
				throw tx;
			}
		}
	}
}
