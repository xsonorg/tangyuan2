package org.xson.tangyuan.manager;

import org.xson.common.object.XCO;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.TangYuanLang;

public abstract class ManagerThreadBase {

	protected Log                    log        = LogFactory.getLog(getClass());

	protected volatile boolean       running    = false;

	protected long                   interval   = 10L * 1000L;

	protected Thread                 t          = null;

	protected String                 threadName = null;

	protected ManagerLauncherContext mlc        = null;

	public void start() {
		this.t = new Thread() {
			@Override
			public void run() {
				ManagerThreadBase.this.run();
			}
		};
		if (null == this.threadName) {
			this.threadName = "-" + getClass().getName() + "-";
		}
		this.t.setName(this.threadName);
		this.t.setDaemon(true);
		this.t.start();
		log.info(TangYuanLang.get("thread.x.started"), this.threadName);
	}

	public void init(Object data) {
	};

	public void update(Object data) {
	}

	public void stop() {
		log.info(TangYuanLang.get("thread.x.stopping"), this.threadName);
		this.running = false;
		//		t.isInterrupted();//TODO
		if (null != t) {
			try {
				t.interrupt();
			} catch (Throwable e) {
			}
		}
	};

	abstract protected void run();

	protected void addTask(Object task) {
	}

	protected void addCommonParam(XCO param) {
		//TODO
	}

	protected String getURL(String name) {
		if (null != mlc) {
			XCO loginResult = mlc.getLoginResult();
			if (null != loginResult) {
				return loginResult.getStringValue(name);
			}
		}
		return null;
	}

	protected Object post(String url, String param) {
		// 1. http request
		// 2. string-->xco
		// 3. check code
		// 4. return xco.getData
		// TODO
		return null;
	}
}
