package org.xson.tangyuan.manager.trace;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.xson.common.object.XCO;
import org.xson.tangyuan.manager.ManagerLauncherContext;
import org.xson.tangyuan.manager.ManagerThreadBase;

public class TraceReporterThread extends ManagerThreadBase {

	private TraceVo            traceVo   = null;

	private LinkedList<Object> taskQueue = new LinkedList<Object>();

	//	public TraceReporterThread(String name, ManagerLauncherContext mlc, TraceVo traceVo) {
	//		if (null == name) {
	//			name = "-TraceReporterThread-";
	//		}
	//		this.threadName = name;
	//		this.mlc = mlc;
	//		this.traceVo = traceVo;
	//	}

	public TraceReporterThread(String name, ManagerLauncherContext mlc) {
		if (null == name) {
			name = "-TraceReporterThread-";
		}
		this.threadName = name;
		this.mlc = mlc;
	}

	@Override
	public void update(Object data) {
		this.traceVo = (TraceVo) data;
	}

	@Override
	public void addTask(Object task) {
		if (running) {
			synchronized (taskQueue) {
				taskQueue.add(task);
			}
		}
	}

	@Override
	public void run() {
		while (running) {
			try {
				Thread.sleep(this.traceVo.getReporterInterval());
			} catch (InterruptedException e) {
				log.error(null, e);
				break;
			}
			doReport();
		}
		long start = System.currentTimeMillis();
		while (taskQueue.size() > 0) {
			if ((System.currentTimeMillis() - start) > this.traceVo.getLastWaitTime()) {
				return;
			}
			log.info("waiting for TraceReporterThread reporter...");
			doReport();
		}
	}

	private void doReport() {
		int       count    = 0;
		Object    task     = null;
		List<XCO> taskList = new ArrayList<XCO>();
		int       batchNum = traceVo.getReporterBatch();
		do {
			// 需要优化批次
			synchronized (taskQueue) {
				task = taskQueue.poll();
			}
			if (null != task) {
				taskList.add((XCO) task);
				count++;
			} else {
				count = batchNum;
			}
			// } while (running && count < reporterBatch);
		} while (count < batchNum);
		if (taskList.size() > 0) {
			doReportBatch(taskList);
		}
	}

	private void doReportBatch(List<XCO> taskList) {
		String url = getURL("report_trace_url");
		if (null == url) {
			return;
		}
		XCO param = new XCO();
		// TODO
		param.setXCOListValue("dataList", taskList);
		post(url, param.toXMLString());
	}

}
