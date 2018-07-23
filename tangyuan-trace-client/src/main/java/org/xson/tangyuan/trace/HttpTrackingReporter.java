package org.xson.tangyuan.trace;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.xson.common.object.XCO;
import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.util.TangYuanAssert;
import org.xson.tools.httpclient.HttpClientFactory;

public class HttpTrackingReporter implements TrackingReporter {

	private Log					log					= LogFactory.getLog(HttpTrackingReporter.class);

	private LinkedList<Object>	taskQueue			= null;
	private volatile boolean	running				= false;
	private ReporterThread		thread				= null;

	private HttpClientFactory	factory				= null;

	/** 上报地址 */
	private String				host				= null;
	/** 批量上报 */
	private int					reporterBatch		= 1;
	/** 上报间隔(秒) */
	private long				reporterInterval	= 1L * 1000L;
	/** 最后等待时间(秒) */
	private long				lastWaitTime		= 10L * 1000L;

	public void init(TrackingConfig config) {
		Properties p = config.getProperties();
		this.host = StringUtils.trim(p.getProperty("host"));
		TangYuanAssert.stringEmpty(this.host, "The 'host' attribute is missing in the trace-config configuration file.");

		if (p.containsKey("reporter.batch")) {
			this.reporterBatch = Integer.parseInt(p.getProperty("reporter.batch"));
		}
		if (p.containsKey("reporter.interval")) {
			this.reporterInterval = Long.parseLong(p.getProperty("reporter.interval")) * 1000L;
		}
		if (p.containsKey("lastWaitTime")) {
			//			this.lastWaitTime = Long.parseLong(p.getProperty("lastWaitTime")) * 1000L;
			this.lastWaitTime = Long.parseLong(p.getProperty("lastWaitTime"));
			if (this.lastWaitTime > TangYuanContainer.getInstance().getMaxWaitTimeForShutDown()) {
				this.lastWaitTime = TangYuanContainer.getInstance().getMaxWaitTimeForShutDown();
			}
			this.lastWaitTime = this.lastWaitTime * 1000L;
		}

		this.factory = new HttpClientFactory(false);
	}

	@Override
	public void start(TrackingConfig config) throws Throwable {
		if (!running) {
			init(config);
			taskQueue = new LinkedList<Object>();
			running = true;
			thread = new ReporterThread();
			thread.start();
			log.info("http tracking reporter start...");
		}
	}

	@Override
	public void stop() {
		if (running) {
			running = false;
			try {
				thread.join();
			} catch (InterruptedException e) {
				log.error("", e);
				thread.interrupt();
			}
			log.info("http tracking reporter stop...");
		}
	}

	@Override
	public void report(Object data) {
		if (running) {
			synchronized (taskQueue) {
				taskQueue.add(data);
			}
		} else {
			log.error("Thread has not been started");
		}
	}

	private class ReporterThread extends Thread {

		public ReporterThread() {
			super("ReporterThread::");
			// setDaemon(true);
		}

		@Override
		public void run() {
			while (running) {
				try {
					sleep(reporterInterval);
				} catch (InterruptedException e) {
					log.error(null, e);
					break;
				}
				collect();
			}

			long start = System.currentTimeMillis();
			while (taskQueue.size() > 0) {
				if ((System.currentTimeMillis() - start) > lastWaitTime) {
					return;
				}
				log.info("waiting for http tracking reporter...");
				collect();
			}

			if (null != factory) {
				factory.shutdown();
			}
		}

		private void collect() {
			int count = 0;
			Object task = null;
			List<XCO> taskList = new ArrayList<XCO>();
			do {
				synchronized (taskQueue) {
					task = taskQueue.poll();
				}
				if (null != task) {
					taskList.add((XCO) task);
					count++;
				} else {
					count = reporterBatch;
				}
				//			} while (running && count < reporterBatch);
			} while (count < reporterBatch);
			if (taskList.size() > 0) {
				doReport(taskList);
			}
		}

		private void doReport(List<XCO> taskList) {
			XCO request = new XCO();
			request.setXCOListValue("dataList", taskList);
			//			System.out.println("doReport size: " + taskList.size());
			try {
				sendPostRequest(host, request.toXMLString().getBytes("UTF-8"), "application/xco");
			} catch (Throwable e) {
				log.error("do report error: " + request, e);
			}
		}
	}

	private String sendPostRequest(String url, byte[] buffer, String contentType) throws Throwable {
		//		XHttpClient xHttpClient = this.factory.getPoolingHttpClient();
		CloseableHttpClient xHttpClient = (CloseableHttpClient) this.factory.getHttpClient();
		try {
			URI uri = new URI(url);
			HttpPost httpost = new HttpPost(uri);
			ByteArrayEntity byteArrayEntity = new ByteArrayEntity(buffer, ContentType.create(contentType, "UTF-8"));
			httpost.setEntity(byteArrayEntity);
			CloseableHttpResponse response = (CloseableHttpResponse) xHttpClient.execute(httpost);
			try {
				int status = response.getStatusLine().getStatusCode();
				if (status != 200) {
					throw new TangYuanException("Unexpected response status: " + status);
				}
				HttpEntity entity = response.getEntity();
				return EntityUtils.toString(entity, "UTF-8");
			} finally {
				response.close();
			}
		} finally {
			xHttpClient.close();
		}
	}

}
