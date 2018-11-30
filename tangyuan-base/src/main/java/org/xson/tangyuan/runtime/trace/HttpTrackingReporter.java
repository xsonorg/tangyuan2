package org.xson.tangyuan.runtime.trace;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.httpclient.HttpClientManager;
import org.xson.tangyuan.httpclient.XHttpClient;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;

public class HttpTrackingReporter implements TrackingReporter {

	private Log					log			= LogFactory.getLog(HttpTrackingReporter.class);

	private LinkedList<Object>	taskQueue	= null;
	private volatile boolean	running		= false;
	private ReporterThread		thread		= null;

	private TrackingConfig		config		= null;

	private XHttpClient			xHttpClient	= null;

	@Override
	public void start(TrackingConfig config) throws Throwable {
		if (!running) {
			HttpClientManager.checkKey(config.getClientId());
			this.config = config;
			this.xHttpClient = HttpClientManager.getXHttpClient(this.config.getClientId());
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
			xHttpClient = null;
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
					sleep(config.getReporterInterval());
				} catch (InterruptedException e) {
					log.error(null, e);
					break;
				}
				collect();
			}

			long start = System.currentTimeMillis();
			while (taskQueue.size() > 0) {
				if ((System.currentTimeMillis() - start) > config.getLastWaitTime()) {
					return;
				}
				log.info("waiting for http tracking reporter...");
				collect();
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
					count = config.getReporterBatch();
				}
				// } while (running && count < reporterBatch);
			} while (count < config.getReporterBatch());
			if (taskList.size() > 0) {
				doReport(taskList);
			}
		}

		private void doReport(List<XCO> taskList) {
			XCO request = new XCO();
			request.setXCOListValue("dataList", taskList);
			try {
				sendPostRequest(config.getHost(), request.toXMLString().getBytes("UTF-8"), "application/xco");
			} catch (Throwable e) {
				log.error("do report error: " + request, e);
			}
		}
	}

	private String sendPostRequest(String url, byte[] buffer, String contentType) throws Throwable {
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
