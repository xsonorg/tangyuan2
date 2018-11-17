package org.xson.tangyuan.mq.datasource.activemq;

import java.util.LinkedList;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.mq.datasource.MqSource;
import org.xson.tangyuan.mq.datasource.MqSourceVo;

public class ActiveMqSource implements MqSource {

	private static Log				log					= LogFactory.getLog(ActiveMqSource.class);

	private ConnectionFactory		connectionFactory	= null;
	private Connection				connection			= null;
	private LinkedList<Connection>	connectionQueue		= null;
	// private LinkedList<Session> sessionQueue = null;

	private LinkedList<Session>		txSessionQueue		= null;
	private LinkedList<Session>		noTxsessionQueue	= null;
	private Object					sessionQueueObject	= null;

	private boolean					poolConnection		= false;
	private boolean					poolSession			= false;

	private int						maxConnections		= 1;
	private int						maxSessions			= 1;

	public void init(MqSourceVo msVo) throws Throwable {

		Map<String, String> properties = msVo.getProperties();

		String brokerURL = properties.get("url".toUpperCase());
		String userName = properties.get("userName".toUpperCase());
		String password = properties.get("password".toUpperCase());

		if (properties.containsKey("maxConnections".toUpperCase())) {
			this.maxConnections = Integer.parseInt(properties.get("maxConnections".toUpperCase()));
		}

		if (properties.containsKey("maxSessions".toUpperCase())) {
			this.maxSessions = Integer.parseInt(properties.get("maxSessions".toUpperCase()));
		}

		// TODO max and min

		// String userName, String password, URI brokerURL
		connectionFactory = new ActiveMQConnectionFactory(userName, password, brokerURL);
		if (maxConnections > 1) {
			connectionQueue = new LinkedList<Connection>();
			for (int i = 0; i < maxConnections; i++) {
				Connection conn = connectionFactory.createConnection();
				conn.start();
				connectionQueue.add(conn);
			}
			poolConnection = true;
		} else {
			connection = connectionFactory.createConnection();
			connection.start();
		}

		if (maxSessions > 1) {
			// sessionQueue = new LinkedList<Session>();

			// private LinkedList<Session> txSessionQueue = null;
			// private LinkedList<Session> noTxsessionQueue = null;
			// private Object sessionQueueObject = null;

			txSessionQueue = new LinkedList<Session>();
			noTxsessionQueue = new LinkedList<Session>();
			sessionQueueObject = new Object();

			poolSession = true;
		}

		log.info("ActiveMq init success, url: " + brokerURL);
	}

	public Connection getConnection() {
		if (poolConnection) {
			Connection conn = null;
			synchronized (connectionQueue) {
				conn = connectionQueue.poll();
				connectionQueue.add(conn);
			}
			return conn;
		} else {
			return connection;
		}
	}

	public Connection getConnection(String clientID) throws Throwable {
		Connection conn = connectionFactory.createConnection();
		conn.setClientID(clientID); // 持久订阅需要设置这个。
		conn.start();
		return conn;
	}

	// public Session getSession(boolean transacted, int acknowledgeMode) throws Throwable {
	// if (poolSession) {
	// Session session = null;
	// // TODO session可能事务不同
	// synchronized (sessionQueue) {
	// session = sessionQueue.poll();
	// }
	// if (null == session) {
	// Connection conn = getConnection();
	// session = conn.createSession(transacted, acknowledgeMode);
	// }
	// return session;
	// }
	// return null;
	// }

	public Session getSession(boolean transacted, int acknowledgeMode) throws Throwable {
		if (poolSession) {
			Session session = null;
			synchronized (sessionQueueObject) {
				if (transacted) {
					session = txSessionQueue.poll();
				} else {
					session = noTxsessionQueue.poll();
				}
			}
			if (null == session) {
				Connection conn = getConnection();
				session = conn.createSession(transacted, acknowledgeMode);
			}
			return session;
		}
		return null;
	}

	// public void closeSession(Session session) {
	// boolean closed = true;
	// if (poolSession) {
	// synchronized (sessionQueue) {
	// if (maxSessions > sessionQueue.size()) {
	// sessionQueue.add(session);
	// closed = false;
	// }
	// }
	// }
	// if (closed) {
	// if (null != session) {
	// try {
	// session.close();
	// } catch (JMSException e) {
	// log.error("ActiveMq session close error.", e);
	// }
	// }
	// }
	// }

	public void closeSession(Session session, boolean transacted) {
		boolean closed = true;
		if (poolSession) {
			synchronized (sessionQueueObject) {
				if (maxSessions > (txSessionQueue.size() + noTxsessionQueue.size())) {
					if (transacted) {
						txSessionQueue.add(session);
					} else {
						noTxsessionQueue.add(session);
					}
					closed = false;
				}
			}
		}
		if (closed) {
			if (null != session) {
				try {
					session.close();
				} catch (JMSException e) {
					log.error("ActiveMq session close error.", e);
				}
			}
		}
	}

	public void close() {
		// if (null != sessionQueue) {
		// for (Session session : sessionQueue) {
		// try {
		// session.close();
		// } catch (Throwable e) {
		// log.error("ActiveMq session close error close.", e);
		// }
		// }
		// }
		if (null != sessionQueueObject) {
			for (Session session : txSessionQueue) {
				try {
					session.close();
				} catch (Throwable e) {
					log.error("ActiveMq session close error.", e);
				}
			}
			for (Session session : noTxsessionQueue) {
				try {
					session.close();
				} catch (Throwable e) {
					log.error("ActiveMq session close error.", e);
				}
			}
		}
		if (null != connection) {
			try {
				connection.close();
			} catch (Throwable e) {
				log.error("ActiveMq connection close error.", e);
			}
		} else {
			for (Connection conn : connectionQueue) {
				try {
					conn.close();
				} catch (Throwable e) {
					log.error("ActiveMq connection close error.", e);
				}
			}
		}
	}
}
