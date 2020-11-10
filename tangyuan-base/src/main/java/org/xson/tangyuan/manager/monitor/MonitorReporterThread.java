package org.xson.tangyuan.manager.monitor;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.xson.common.object.XCO;
import org.xson.tangyuan.manager.ManagerLauncherContext;
import org.xson.tangyuan.manager.ManagerThreadBase;
import org.xson.tangyuan.util.DateUtils;
import org.xson.tangyuan.util.StringUtils;

public class MonitorReporterThread extends ManagerThreadBase {

	private String             token     = null;

	private LinkedList<Object> taskQueue = new LinkedList<Object>();

	private MonitorVo          monitorVo = null;

	//	public MonitorReporterThread(String name, ManagerLauncherContext mlc, MonitorVo monitorVo) {
	//		if (null == name) {
	//			name = "-MonitorReporterThread-";
	//		}
	//		this.threadName = name;
	//		this.mlc = mlc;
	//		this.monitorVo = monitorVo;
	//	}

	public MonitorReporterThread(String name, ManagerLauncherContext mlc) {
		if (null == name) {
			name = "-MonitorReporterThread-";
		}
		this.threadName = name;
		this.mlc = mlc;
	}

	@Override
	public void update(Object data) {
		// TODO 线程安全问题
		this.monitorVo = (MonitorVo) data;
	}

	@Override
	public void addTask(Object task) {
		if (running) {
			if (this.monitorVo.isEnableServiceReport()) {
				synchronized (taskQueue) {
					taskQueue.add(task);
				}
			}
		}
	}

	@Override
	public void run() {
		this.reportNode();
		this.reportApp();
		while (running) {
			try {
				Thread.sleep(monitorVo.getReporterInterval());
			} catch (InterruptedException e) {
				log.error(e);
				break;
			}
			reportInfo();
		}
		this.reportShutdown();
	}

	private void reportInfo() {
		if (this.monitorVo.isEnableServiceReport()) {
			LinkedList<Object> tempTaskQueue = null;
			synchronized (taskQueue) {
				if (this.taskQueue.size() > 0) {
					tempTaskQueue = this.taskQueue;
					this.taskQueue = new LinkedList<Object>();
				}
			}
			if (null != tempTaskQueue) {
				reportServicePrefStat(tempTaskQueue);
			}
		}
		// TODO是否要合并在一起
		//			this.reportJvm();
		//			this.reportServicePrefStat();
		if (this.monitorVo.isEnableJvmReport()) {
			reportJvm();
		}

	}

	/**
	 * 节点报告: 启动(1)
	 */
	private void reportNode() {
		XCO xco = getNodeInfo();
		// TODO add other
		post(getURL("report_node_url"), xco.toXMLString());
	}

	/**
	 * JVM报告: 周期/启动(1)
	 */
	private void reportJvm() {
		XCO xco = getJVMInfo();
		// TODO add other
		post(getURL("report_jvm_url"), xco.toXMLString());
	}

	/**
	 * App信息报告: 启动(1)
	 */
	private void reportApp() {//Host
		XCO xco = getAppInfo();
		// TODO add other
		post(getURL("report_app_url"), xco.toXMLString());
	}

	/**
	 * 服务性能报告(Service performance statistics): 周期
	 */
	private void reportServicePrefStat(List<Object> queue) {
		XCO xco = getServicePrefStatInfo(queue);
		// TODO add other
		post(getURL("report_service_url"), xco.toXMLString());
	}

	/**
	 * 服务报告: 关闭(1)
	 */
	private void reportShutdown() {
		XCO xco = getShutdownInfo();
		// TODO add other
		post(getURL("report_shudown_url"), xco.toXMLString());
	}

	private double getDouble(long val) {
		return new BigDecimal(new Double(val).doubleValue() / 1024L / 1024L).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	///////////////////////////////////////////////////////////////////////////

	private XCO getNodeInfo() {
		XCO           xco    = new XCO();
		RuntimeMXBean mxbean = ManagementFactory.getRuntimeMXBean();
		//		xco.setStringValue("node_name", this.nodeName);//TODO
		xco.setStringValue("jvm_v", StringUtils.trim(mxbean.getVmVersion()));
		xco.setStringValue("start_time", DateUtils.getDateTimeString(new Date()));
		return xco;
	}

	private XCO getAppInfo() {
		XCO       xco      = new XCO();
		//		xco.setStringValue("node_name", nodeName);// TODO
		List<XCO> hostList = new ArrayList<XCO>();
		XCO       hXCO     = new XCO();
		//		Set<String> serviceList = (Set<String>) sh.getServiceInfo();
		//		hXCO.setStringValue("host_id", nodeName + "_" + domain);
		//		hXCO.setStringValue("node_name", nodeName);
		//		hXCO.setStringValue("host_name", domain);
		//		hXCO.setStringValue("service_num", serviceList.size() + "");
		//		hXCO.setStringListValue("serviceList", getServiceList(serviceList));
		hostList.add(hXCO);
		// add Host ID
		//		hostIdList.add(nodeName + "_" + domain);// TODO
		// add MD5(services) // TODO
		xco.setXCOListValue("hostList", hostList);
		//		return xco;
		return xco;
	}

	private XCO getShutdownInfo() {
		XCO xco = new XCO();
		xco.setDateTimeValue("update_time", new Date());
		//		xco.setStringValue("node_name", this.nodeName);// TODO
		return xco;
	}

	private XCO getJVMInfo() {
		XCO           xco    = new XCO();
		RuntimeMXBean mxbean = ManagementFactory.getRuntimeMXBean();
		xco.setStringValue("jvm_v", StringUtils.trim(mxbean.getVmVersion()));

		MemoryMXBean mem  = ManagementFactory.getMemoryMXBean();
		MemoryUsage  heap = mem.getHeapMemoryUsage();

		xco.setDoubleValue("mem_init", getDouble(heap.getInit()));
		xco.setDoubleValue("mem_committed", getDouble(heap.getCommitted()));
		xco.setDoubleValue("mem_used", getDouble(heap.getUsed()));
		xco.setDoubleValue("mem_max", getDouble(heap.getMax()));

		ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();

		xco.setIntegerValue("thread_count", threadMxBean.getThreadCount());
		List<XCO>              mpList = new ArrayList<XCO>();
		List<MemoryPoolMXBean> mps    = ManagementFactory.getMemoryPoolMXBeans();
		for (MemoryPoolMXBean mp : mps) {
			XCO         mpXCO    = new XCO();
			XCO         innerMem = new XCO();
			MemoryUsage tempMU   = mp.getCollectionUsage();
			if (null != tempMU) {
				innerMem.setDoubleValue("mem_init", getDouble(tempMU.getInit()));
				innerMem.setDoubleValue("mem_committed", getDouble(tempMU.getCommitted()));
				innerMem.setDoubleValue("mem_used", getDouble(tempMU.getUsed()));
				innerMem.setDoubleValue("mem_max", getDouble(tempMU.getMax()));
			}
			mpXCO.setStringValue("name", StringUtils.trim(mp.getName()));
			mpXCO.setStringValue("type", StringUtils.trim(mp.getType().toString()));
			mpXCO.setXCOValue("mem", innerMem);
			mpList.add(mpXCO);
		}
		xco.setXCOListValue("mpList", mpList);
		//		xco.setStringValue("node_name", this.nodeName);// TODO
		return xco;
	}

	private XCO getServicePrefStatInfo(List<Object> queue) {
		return null;//TODO
	}
}
