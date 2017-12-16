package org.xson.tangyuan.hbase.datasource;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.xson.logging.Log;
import org.xson.logging.LogFactory;

public class HBaseDataSource {

	private Log				log					= LogFactory.getLog(HBaseDataSource.class);

	private DataSourceVo	vo					= null;
	private Configuration	hbaseConfig			= null;
	private Connection		conn				= null;

	private ExecutorService	pool				= null;
	private Integer			threadPoolNumber	= null;

	public HBaseDataSource(DataSourceVo vo) {
		this.vo = vo;
	}

	public void init() throws Throwable {

		hbaseConfig = HBaseConfiguration.create();

		// hbaseConfig.set("hbase.zookeeper.quorum","10.10.3.181,10.10.3.182,10.10.3.183");
		// hbaseConfig.set("hbase.zookeeper.property.clientPort","2181");
		// hbaseConfig.set("zookeeper.znode.parent","/hbase");

		Map<String, String> properties = this.vo.getProperties();

		if (null != properties && properties.size() > 0) {

			// 先处理框架自身的变量设置,然后剩余的都是HBase的
			String key = "thread.pool.number";
			if (properties.containsKey(key)) {
				this.threadPoolNumber = Integer.parseInt(properties.get(key));
				properties.remove(key);
			}

			for (Entry<String, String> item : properties.entrySet()) {
				hbaseConfig.set(item.getKey(), item.getValue());
			}
		}

		createConnection();
	}

	private synchronized void createConnection() throws IOException {
		if (null != this.threadPoolNumber) {
			this.pool = Executors.newFixedThreadPool(this.threadPoolNumber);
		}
		if (null == this.pool) {
			this.conn = ConnectionFactory.createConnection(hbaseConfig);
		} else {
			this.conn = ConnectionFactory.createConnection(hbaseConfig, this.pool);
		}

	}

	public String getId() {
		return this.vo.getId();
	}

	public void stop() {
		try {
			if (null != this.pool) {
				this.pool.shutdownNow();
				this.pool = null;
			}
		} catch (Throwable e) {
			log.error("this.pool.shutdownNow().", e);
		}
		try {
			if (null != this.conn) {
				this.conn.close();
				this.conn = null;
			}
		} catch (Throwable e) {
			log.error("conn.close error.", e);
		}
	}

	public synchronized Connection getConnection() throws IOException {
		if (null == this.conn || this.conn.isClosed()) {
			stop();
			createConnection();
		}
		return this.conn;
	}

	public void recycleConnection(Connection connection) {
	}
}
