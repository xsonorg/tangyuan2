package org.xson.tangyuan.mq.executor.rabbitmq;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.mq.MqComponent;
import org.xson.tangyuan.mq.datasource.rabbitmq.RabbitMqSource;
import org.xson.tangyuan.mq.executor.Sender;
import org.xson.tangyuan.mq.service.context.MqServiceContext;
import org.xson.tangyuan.mq.vo.ChannelVo;
import org.xson.tangyuan.mq.vo.ChannelVo.ChannelType;
import org.xson.tangyuan.mq.vo.RabbitMqChannelVo;
import org.xson.tangyuan.mq.vo.RoutingVo;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;

public class RabbitMqSender extends Sender {

	private Log log = LogFactory.getLog(getClass());

	@Override
	public void sendMessage(ChannelVo queue, RoutingVo rVo, Object arg, boolean useTx, MqServiceContext context) throws Throwable {
		if (ChannelType.Queue == queue.getType()) {
			sendQueueMessage((RabbitMqChannelVo) queue, rVo, arg, useTx, context);
		} else {// Topic
			sendTopicMessage((RabbitMqChannelVo) queue, rVo, arg, useTx, context);
		}
	}

	private void sendQueueMessage(RabbitMqChannelVo queue, RoutingVo rVo, Object arg, boolean useTx, MqServiceContext context) throws Throwable {

		if (!isRouting(rVo, arg)) {
			log.info("The current queue[" + queue.getName() + "] does not match the routing rule and does not send the message.");
			return;
		}

		RabbitMqSource mqSource  = (RabbitMqSource) MqComponent.getInstance().getMqSourceManager().getMqSource(queue.getMsKey());
		Channel        channel   = null;
		Throwable      tx        = null;

		String         queueName = queue.getName();
		try {

			// boolean durable = (Boolean) queue.getProperties().get(RabbitMqVo.RABBITMQ_DURABLE);
			// boolean exclusive = (Boolean) queue.getProperties().get(RabbitMqVo.RABBITMQ_EXCLUSIVE);
			// boolean autoDelete = (Boolean) queue.getProperties().get(RabbitMqVo.RABBITMQ_AUTODELETE);

			boolean durable    = queue.isDurable();
			boolean exclusive  = queue.isExclusive();
			boolean autoDelete = queue.isAutoDelete();

			channel = mqSource.getChannel();
			channel.queueDeclare(queueName, durable, exclusive, autoDelete, null);
			if (useTx) {
				channel.txSelect();
				context.addTransactionObject(new RabbitMqTransactionObject(mqSource, channel));
			}

			byte[] body = getBody(arg);
			channel.basicPublish("", queueName, null, body);
			log.info("send message to queue[" + queueName + "]: " + ((XCO) arg).toXMLString());
		} catch (Throwable e) {
			tx = e;
		} finally {
			if (null != channel && !useTx) {
				// closeChannel(channel);
				mqSource.closeChannel(channel);
			}
			if (null != tx) {
				throw tx;
			}
		}
	}

	private void sendTopicMessage(RabbitMqChannelVo queue, RoutingVo rVo, Object arg, boolean useTx, MqServiceContext context) throws Throwable {
		RabbitMqSource mqSource = (RabbitMqSource) MqComponent.getInstance().getMqSourceManager().getMqSource(queue.getMsKey());
		Channel        channel  = null;
		Throwable      tx       = null;
		String         exchange = queue.getName();
		try {
			// String exchangeType = (String) queue.getProperties().get(RabbitMqVo.RABBITMQ_EXCHANGE_TYPE);
			String exchangeType = queue.getExchangeType();

			String routingKey   = null;
			if (BuiltinExchangeType.FANOUT.getType().equalsIgnoreCase(exchangeType)) {
				routingKey = "";
			} else {
				routingKey = getExchangeRoutingKey(rVo, arg);
			}

			if (null == routingKey) {
				throw new TangYuanException("when sending a topic[" + exchange + "], the routingKey is empty, arg: " + arg);
			}

			channel = mqSource.getChannel();
			channel.exchangeDeclare(exchange, exchangeType);

			if (useTx) {
				channel.txSelect();
				context.addTransactionObject(new RabbitMqTransactionObject(mqSource, channel));
			}

			byte[] body = getBody(arg);
			channel.basicPublish(exchange, routingKey, null, body);

			// log.info("send message to topic[" + exchange + "]: " + ((XCO) arg).toXMLString());
			log.info("send message to exchange[" + exchange + "]: " + ((XCO) arg).toXMLString());
		} catch (Throwable e) {
			tx = e;
		} finally {
			if (null != channel && !useTx) {
				// closeChannel(channel);
				mqSource.closeChannel(channel);
			}
			if (null != tx) {
				throw tx;
			}
		}
	}

	// private void closeChannel(Channel channel) {
	// try {
	// channel.close();
	// } catch (Throwable e) {
	// log.error("channel close error.", e);
	// }
	// }

	private byte[] getBody(Object arg) throws Throwable {
		if (arg instanceof XCO) {
			return ((XCO) arg).toXMLString().getBytes("UTF-8");// TODO
		}
		throw new TangYuanException("Unsupported type: " + arg.getClass().getName());
	}

	private String getExchangeRoutingKey(RoutingVo rVo, Object arg) {
		if (null == rVo) {
			return null;
		}
		if (null != rVo.getKey()) {
			return getRoutingKey(rVo, arg);
		}
		return rVo.getPattern();
	}
}
