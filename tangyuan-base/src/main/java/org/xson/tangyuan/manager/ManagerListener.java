package org.xson.tangyuan.manager;

import java.util.List;

import org.xson.common.object.XCO;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.TangYuanLang;

/**
 * 监听器，监听服务端变化
 */
public class ManagerListener extends ManagerThreadBase {

	private Log               log    = LogFactory.getLog(getClass());

	private String            token  = null;

	private ManagerThreadBase client = null;

	public ManagerListener(String name, ManagerThreadBase client) {
		if (null == name) {
			name = "-ManagerListener-";
		}
		this.threadName = name;
		this.client = client;
	}

	@Override
	protected void run() {
		while (running) {
			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				log.error(null, e);
				break;
			}
			listen();
		}
		log.info(TangYuanLang.get("thread.x.ended"), this.threadName);
	}

	@SuppressWarnings("unchecked")
	private void listen() {
		try {
			String    url      = getURL("listen_url");
			List<XCO> tasklist = (List<XCO>) post(url, new XCO().toXMLString());
			if (null != tasklist) {
				process(tasklist);
			}
		} catch (Throwable e) {
			log.error(e);
		}
	}

	private void process(List<XCO> tasklist) {
		for (XCO task : tasklist) {
			if (null != client) {
				client.addTask(task);
			}
		}
	}
}
