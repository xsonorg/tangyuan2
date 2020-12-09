package org.xson.tangyuan.mq.executor.rabbitmq;

import java.io.IOException;
import java.util.List;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.mq.MqComponent;
import org.xson.tangyuan.mq.datasource.rabbitmq.RabbitMqSource;
import org.xson.tangyuan.mq.executor.Receiver;
import org.xson.tangyuan.mq.vo.BindingPattern;
import org.xson.tangyuan.mq.vo.BindingVo;
import org.xson.tangyuan.mq.vo.ChannelVo;
import org.xson.tangyuan.mq.vo.ChannelVo.ChannelType;
import org.xson.tangyuan.mq.vo.RabbitMqChannelVo;
import org.xson.tangyuan.service.runtime.RuntimeContext;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Envelope;

public class RabbitMqReceiver extends Receiver {

	private static Log         log               = LogFactory.getLog(RabbitMqReceiver.class);

	private volatile boolean   running           = false;
	protected volatile boolean closed            = false;

	private RabbitMqSource     mqSource          = null;
	private Channel            channel           = null;

	private String             service           = null;
	private RabbitMqChannelVo  queue             = null;
	private BindingVo          binding           = null;

	private String             typeStr           = null;
	// 同步接收线程
	private Thread             syncReceiveThread = null;

	public RabbitMqReceiver(String service, ChannelVo queue, BindingVo binding) {
		this.service = service;
		this.queue = (RabbitMqChannelVo) queue;
		this.binding = binding;
	}

	@Override
	public void start() throws Throwable {
		if (ChannelType.Queue == queue.getType()) {
			typeStr = "queue";
			recvQueueMessage();
		} else {
			typeStr = "topic";
			recvTopicMessage();
		}
	}

	private void recvQueueMessage() throws Throwable {

		String        queueName     = queue.getName();
		// boolean durable = (Boolean) queue.getProperties().get(RabbitMqVo.RABBITMQ_DURABLE);
		// boolean exclusive = (Boolean) queue.getProperties().get(RabbitMqVo.RABBITMQ_EXCLUSIVE);
		// boolean autoDelete = (Boolean) queue.getProperties().get(RabbitMqVo.RABBITMQ_AUTODELETE);
		// int prefetchCount = (Integer) queue.getProperties().get(RabbitMqVo.RABBITMQ_PREFETCHCOUNT);
		// final boolean autoAck = (Boolean) queue.getProperties().get(RabbitMqVo.RABBITMQ_AUTOACK);

		boolean       durable       = queue.isDurable();
		boolean       exclusive     = queue.isExclusive();
		boolean       autoDelete    = queue.isAutoDelete();

		int           prefetchCount = queue.getPrefetchCount();
		final boolean autoAck       = queue.isAutoAck();

		// RabbitMqSource mqSource = (RabbitMqSource) MqContainer.getInstance().getMqSourceManager().getMqSource(queue.getMsKey());
		mqSource = (RabbitMqSource) MqComponent.getInstance().getMqSourceManager().getMqSource(queue.getMsKey());
		channel = mqSource.getChannel();
		channel.queueDeclare(queueName, durable, exclusive, autoDelete, null);
		channel.basicQos(prefetchCount);

		running = true;

		// boolean asynReceiveMessages = (Boolean) queue.getProperties().get(RabbitMqVo.RABBITMQ_C_ASYNRECEIVEMESSAGES);
		boolean asynReceiveMessages = queue.isAsynReceive();
		recv(asynReceiveMessages, queueName, autoAck, binding);
	}

	private void recvTopicMessage() throws Throwable {

		String        exchange     = queue.getName();
		// String exchangeType = (String) queue.getProperties().get(RabbitMqVo.RABBITMQ_EXCHANGE_TYPE);
		// final boolean autoAck = (Boolean) queue.getProperties().get(RabbitMqVo.RABBITMQ_AUTOACK);

		String        exchangeType = queue.getExchangeType();
		final boolean autoAck      = queue.isAutoAck();

		// RabbitMqSource mqSource = (RabbitMqSource) MqContainer.getInstance().getMqSourceManager().getMqSource(queue.getMsKey());
		mqSource = (RabbitMqSource) MqComponent.getInstance().getMqSourceManager().getMqSource(queue.getMsKey());
		channel = mqSource.getChannel();
		channel.exchangeDeclare(exchange, exchangeType);
		String queueName = channel.queueDeclare().getQueue();

		if (BuiltinExchangeType.FANOUT.getType().equalsIgnoreCase(exchangeType)) {
			channel.queueBind(queueName, exchange, "");
		} else {
			if (null == binding) {
				throw new TangYuanException("exchange[" + exchange + "], the binding is empty.");
			}
			List<BindingPattern> patterns = binding.getPatterns();
			if (null == patterns || 0 == patterns.size()) {
				throw new TangYuanException("exchange[" + exchange + "], the binding is empty.");
			}
			for (BindingPattern bp : patterns) {
				channel.queueBind(queueName, exchange, bp.getPattern());
			}
		}
		// channel.basicQos(prefetchCount);// TODO
		running = true;
		// boolean asynReceiveMessages = (Boolean) queue.getProperties().get(RabbitMqVo.RABBITMQ_C_ASYNRECEIVEMESSAGES);
		boolean asynReceiveMessages = queue.isAsynReceive();
		recv(asynReceiveMessages, queueName, autoAck, null);
	}

	private void recv(boolean asynReceiveMessages, String queueName, final boolean autoAck, final BindingVo binding) throws Throwable {
		if (asynReceiveMessages) {
			Consumer consumer = new DefaultConsumer(channel) {
				@Override
				public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException {
					try {
						XCO xcoMessage = getMessage(body);

						// 添加上下文记录
						// TODO
						// RuntimeContext.beginFromArg(xcoMessage, RuntimeContext.CONTEXT_ORIGIN_MQ);

						log.info("received a message from " + typeStr + "[" + queue.getName() + "]: " + xcoMessage);
						boolean result = exec(service, xcoMessage, binding);
						if (!autoAck && result) {
							channel.basicAck(envelope.getDeliveryTag(), false);
						}
					} catch (Throwable e) {
						log.error("listen to the [" + queue.getName() + "] error.", e);
						// TODO 可能会出现断链的问题
					} finally {
						// 清理上下文记录
						RuntimeContext.clean();
					}
				}
			};
			channel.basicConsume(queueName, autoAck, consumer);
		} else {
			//			QueueingConsumer consumer = new QueueingConsumer(channel);
			//			channel.basicConsume(queueName, autoAck, consumer);
			//			startSyncReceiveThread(consumer, autoAck, binding);

			DeliverCallback deliverCallback = (consumerTag, delivery) -> {
				//	            String message = new String(delivery.getBody(), "UTF-8");
				//	            System.out.println(" [x] Received '" + message + "'");
				//	            try {
				//	                doWork(message);
				//	            } finally {
				//	                System.out.println(" [x] Done");
				//	                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
				//	            }

				try {
					XCO xcoMessage = getMessage(delivery.getBody());
					log.info("received a message from " + typeStr + "[" + queue.getName() + "]: " + xcoMessage);
					boolean result = exec(service, xcoMessage, binding);
					if (!autoAck && result) {
						channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}

			};
			//			channel.basicConsume(queueName, autoAck, consumerTag, callback);
			channel.basicConsume(queueName, autoAck, deliverCallback, consumerTag -> {
			});
		}
	}

	//	private void startSyncReceiveThread(final QueueingConsumer consumer, final boolean autoAck, final BindingVo binding) {
	//		syncReceiveThread = new SyncReceiveThread() {
	//			@Override
	//			public void run() {
	//				log.info("start listen to the " + typeStr + "[" + queue.getName() + "].");
	//				while (running) {
	//					try {
	//						QueueingConsumer.Delivery delivery   = consumer.nextDelivery();
	//						XCO                       xcoMessage = getMessage(delivery.getBody());
	//
	//						// 添加上下文记录
	//						// RuntimeContext.beginFromArg(xcoMessage, "MQ");
	//						// RuntimeContext.beginFromArg(xcoMessage, RuntimeContext.CONTEXT_ORIGIN_MQ);
	//						// TODO
	//
	//						log.info("received a message from " + typeStr + "[" + queue.getName() + "]: " + xcoMessage);
	//						boolean result = exec(service, xcoMessage, binding);
	//						if (!autoAck && result) {
	//							channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
	//						}
	//					} catch (ShutdownSignalException e) {
	//						// TODO 可能会出现断链的问题
	//						e.printStackTrace();
	//					} catch (Throwable e) {
	//						log.error("listen to the [" + queue.getName() + "] error.", e);
	//					}
	//					// 清理上下文记录
	//					RuntimeContext.clean();
	//				}
	//				closed = true;
	//			}
	//		};
	//		syncReceiveThread.start();
	//	}

	private XCO getMessage(byte[] body) throws Throwable {
		String message = new String(body, "UTF-8");
		return XCO.fromXML(message);
	}

	@Override
	public Log getLog() {
		return log;
	}

	@Override
	public void stop() {
		this.running = false;

		if (null != syncReceiveThread) {
			syncReceiveThread.interrupt();
		}

		if (null != channel) {
			// try {
			// channel.close();
			// } catch (Throwable e) {
			// log.error("RabbitMq channel close error.", e);
			// }
			mqSource.closeChannel(channel);
		}
	}

}
