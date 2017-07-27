package org.xson.tangyuan.mq.executor.activemq;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.management.modelmbean.XMLParseException;

import org.xson.common.object.XCO;
import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.mq.MqContainer;
import org.xson.tangyuan.mq.datasource.activemq.ActiveMqSource;
import org.xson.tangyuan.mq.executor.Receiver;
import org.xson.tangyuan.mq.vo.ActiveMqChannelVo;
import org.xson.tangyuan.mq.vo.BindingVo;
import org.xson.tangyuan.mq.vo.ChannelVo;
import org.xson.tangyuan.mq.vo.ChannelVo.ChannelType;

// TODO 考虑异常重启的问题
public class ActiveMqReceiver extends Receiver {

	private static Log			log						= LogFactory.getLog(ActiveMqReceiver.class);

	private volatile boolean	running					= false;
	protected volatile boolean	closed					= false;

	private Session				session					= null;

	private String				service					= null;
	// private ChannelVo queue = null;

	private ActiveMqChannelVo	queue					= null;

	private BindingVo			binding					= null;

	private String				typeStr					= null;

	private Connection			durableSubscriberConn	= null;
	// 同步接收线程
	private Thread				syncReceiveThread		= null;

	public ActiveMqReceiver(String service, ChannelVo queue, BindingVo binding) {
		this.service = service;
		this.queue = (ActiveMqChannelVo) queue;
		this.binding = binding;
	}

	@Override
	public void start() throws Throwable {

		boolean durableSubscribers = false;// 持久化订阅
		String clientID = null;
		if (ChannelType.Topic == queue.getType()) {
			// durableSubscribers = (Boolean) queue.getProperties().get(ActiveMqVo.ACTIVEMQ_C_DURABLESUBSCRIBERS);
			durableSubscribers = queue.isDurableSubscribers();
			if (durableSubscribers) {
				// clientID = (String) queue.getProperties().get(ActiveMqVo.ACTIVEMQ_C_CLIENTID);
				clientID = queue.getClientID();
				if (null == clientID) {
					throw new XMLParseException("durable subscribers is missing a clientID: " + queue.getName());
				}
			}
		}

		ActiveMqSource mqSource = (ActiveMqSource) MqContainer.getInstance().getMqSourceManager().getMqSource(queue.getMsKey());

		Connection connection = null;
		if (!durableSubscribers) {
			connection = mqSource.getConnection();
		} else {
			connection = mqSource.getConnection(clientID);
			this.durableSubscriberConn = connection;
		}

		// boolean transacted = (Boolean) queue.getProperties().get(ActiveMqVo.ACTIVEMQ_C_TRANSACTED);
		// int acknowledgeMode = (Integer) queue.getProperties().get(ActiveMqVo.ACTIVEMQ_C_ACKNOWLEDGEMODE);
		boolean transacted = queue.isTransacted();
		int acknowledgeMode = queue.getAcknowledgeMode();
		session = connection.createSession(transacted, acknowledgeMode);

		Destination destination = null;
		if (ChannelType.Queue == queue.getType()) {
			destination = session.createQueue(queue.getName());
			typeStr = "queue";
		} else if (ChannelType.Topic == queue.getType()) {
			destination = session.createTopic(queue.getName());
			typeStr = "topic";
		}

		MessageConsumer messageConsumer = null;
		if (!durableSubscribers) {
			messageConsumer = session.createConsumer(destination);
		} else {
			messageConsumer = session.createDurableSubscriber((Topic) destination, clientID);
		}

		running = true;

		// boolean asynReceiveMessages = (Boolean) queue.getProperties().get(ActiveMqVo.ACTIVEMQ_C_ASYNRECEIVEMESSAGES);
		boolean asynReceiveMessages = queue.isAsynReceive();
		if (asynReceiveMessages) {
			messageConsumer.setMessageListener(new MessageListener() {
				@Override
				public void onMessage(Message message) {
					try {
						// TODO 如果是session.commit();, 是否需要使用同步关键字, 防止提交别的线程的东西
						// System.out.println("####################:" + Thread.currentThread().getName());
						processMessage(message);
					} catch (Throwable e) {
						log.error("listen to the [" + queue.getName() + "] error.", e);
					}
				}
			});
		} else {
			// long receiveTimeout = (Long) queue.getProperties().get(ActiveMqVo.ACTIVEMQ_C_RECEIVETIMEOUT);
			long receiveTimeout = queue.getReceiveTimeout();
			startSyncReceiveThread(messageConsumer, receiveTimeout);
		}
	}

	private void startSyncReceiveThread(final MessageConsumer messageConsumer, final long receiveTimeout) {
		syncReceiveThread = new SyncReceiveThread() {
			@Override
			public void run() {
				log.info("start listen to the " + typeStr + "[" + queue.getName() + "].");
				while (running) {
					try {
						Message message = messageConsumer.receive(receiveTimeout);
						processMessage(message);
					} catch (Throwable e) {
						// 如果是关闭的时候，可能会报InterruptedException
						log.error("listen to the [" + queue.getName() + "] error.", e);
					}
				}
				closed = true;
			}
		};
		syncReceiveThread.start();
	}

	private void processMessage(Message message) throws Throwable {
		if (null == message) {
			// log.error("message is null...");
			return;
		}
		try {
			XCO xcoMessage = null;
			if (message instanceof TextMessage) {
				TextMessage textMessage = (TextMessage) message;
				String text = textMessage.getText();
				xcoMessage = XCO.fromXML(text);
			} else {
				// TODO
			}

			log.info("received a message from " + typeStr + "[" + queue.getName() + "]: " + xcoMessage);

			boolean execResult = true;
			if (null != xcoMessage) {
				execResult = exec(service, xcoMessage, binding);
			}

			if (execResult && Session.SESSION_TRANSACTED == session.getAcknowledgeMode()) {
				session.commit();
			} else if (execResult && Session.CLIENT_ACKNOWLEDGE == session.getAcknowledgeMode()) {
				message.acknowledge();
			}

			if (!execResult && Session.SESSION_TRANSACTED == session.getAcknowledgeMode()) {
				session.rollback();
			}
		} catch (Throwable e) {
			if (Session.SESSION_TRANSACTED == session.getAcknowledgeMode()) {
				session.rollback();
			}
			throw e;
		}
	}

	@Override
	public Log getLog() {
		return log;
	}

	// @SuppressWarnings("static-access")
	@Override
	public void stop() {
		this.running = false;

		if (null != syncReceiveThread) {
			// // 如何优雅的关闭
			// long start = System.currentTimeMillis();
			// // 最大等等10秒
			// long maxWaitTimeForShutDown = 1000L * 10L;
			// log.info("waiting for receive to close.");
			// while (!closed) {
			// if ((System.currentTimeMillis() - start) > maxWaitTimeForShutDown) {
			// break;
			// }
			// try {
			// Thread.currentThread().sleep(500L);
			// } catch (InterruptedException e) {
			// break;
			// }
			// }
			// if (!closed) {
			// syncReceiveThread.interrupt();
			// }
			syncReceiveThread.interrupt();
		}

		if (null != session) {
			try {
				session.close();
			} catch (JMSException e) {
				log.error("ActiveMq session close error.", e);
			}
		}
		if (null != durableSubscriberConn) {
			try {
				durableSubscriberConn.close();
			} catch (JMSException e) {
				log.error("ActiveMq durableSubscriberConn close error.", e);
			}
		}
	}

}
