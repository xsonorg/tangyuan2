package org.xson.tangyuan.mq.datasource.rabbitmq;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;

import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.mq.datasource.MqSource;
import org.xson.tangyuan.mq.datasource.MqSourceVo;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMqSource implements MqSource {

	private static Log				log				= LogFactory.getLog(RabbitMqSource.class);

	private ConnectionFactory		factory			= null;

	private Connection				connection		= null;
	private LinkedList<Connection>	connectionQueue	= null;

	private boolean					poolConnection	= false;
	// private boolean poolSession = false;

	private int						maxConnections	= 1;
	// private int maxSessions = 1;

	public void init(MqSourceVo msVo) throws Throwable {
		Map<String, String> properties = msVo.getProperties();

		if (properties.containsKey("maxConnections".toUpperCase())) {
			this.maxConnections = Integer.parseInt(properties.get("maxConnections".toUpperCase()));
		}

		// if (properties.containsKey("maxSessions".toUpperCase())) {
		// this.maxSessions = Integer.parseInt(properties.get("maxSessions".toUpperCase()));
		// }

		factory = new ConnectionFactory();

		if (properties.containsKey("Host".toUpperCase())) {
			factory.setHost(properties.get("Host".toUpperCase()));
		}
		if (properties.containsKey("Port".toUpperCase())) {
			factory.setPort(Integer.parseInt(properties.get("Port".toUpperCase())));
		}
		if (properties.containsKey("VirtualHost".toUpperCase())) {
			factory.setVirtualHost(properties.get("VirtualHost".toUpperCase()));
		}
		if (properties.containsKey("Username".toUpperCase())) {
			factory.setUsername(properties.get("Username".toUpperCase()));
		}
		if (properties.containsKey("Password".toUpperCase())) {
			factory.setPassword(properties.get("Password".toUpperCase()));
		}

		// factory.setAutomaticRecoveryEnabled(true);
		// factory.setNetworkRecoveryInterval(10000);// attempt recovery every 10 seconds

		// if (maxSessions > 1) {
		// poolSession = true;
		// }

		if (maxConnections > 1) {
			connectionQueue = new LinkedList<Connection>();
			for (int i = 0; i < maxConnections; i++) {
				Connection conn = factory.newConnection();
				connectionQueue.add(conn);
			}
			poolConnection = true;
		} else {
			connection = factory.newConnection();
		}

	}

	public Channel getChannel() throws IOException {
		// return connection.createChannel();
		if (poolConnection) {
			Connection conn = null;
			synchronized (connectionQueue) {
				conn = connectionQueue.poll();
				connectionQueue.add(conn);
			}
			return conn.createChannel();
		} else {
			return connection.createChannel();
		}
	}

	public void closeChannel(Channel channel) {
		try {
			channel.close();
		} catch (Throwable e) {
			log.error("RabbitMq channel close error.", e);
		}
	}

	@Override
	public void close() {
		if (null != connection) {
			try {
				connection.close();
			} catch (IOException e) {
				log.error("RabbitMq connection close error.", e);
			}
		} else {
			for (Connection conn : connectionQueue) {
				try {
					conn.close();
				} catch (Throwable e) {
					log.error("RabbitMq connection close error.", e);
				}
			}
		}
	}

}
