package org.xson.tangyuan.service.mr;

import java.util.ArrayList;
import java.util.List;

import org.xson.common.object.XCO;

/**
 * 默认合并处理器
 */
public class DefaultMapReduceHander implements MapReduceHander {

	private volatile int count = 0;

	private long         sleepTime;//TODO

	private int          total;

	private List<Object> resultList;

	public DefaultMapReduceHander(int total) {
		this(total, 10L);
	}

	public DefaultMapReduceHander(int total, long sleepTime) {
		this.total = total;
		this.sleepTime = sleepTime;
		this.resultList = new ArrayList<Object>();
	}

	//	@Override
	//	public void merge(Object context, String service, Object result) {
	//		synchronized (this) {
	//			count++;
	//			XCO xco = (XCO) result;
	//			if (0 != xco.getCode()) {
	//				return;
	//			}
	//			XCO data = xco.getData();
	//			if (null == data) {
	//				return;
	//			}
	//			this.resultList.add(data);
	//		}
	//	}

	@Override
	public void merge(Object context, String service, Object result) {
		synchronized (this) {
			count++;
			XCO xco = (XCO) result;
			if (0 != xco.getCode()) {
				return;
			}
			Object data = xco.getData();
			if (null == data) {
				return;
			}
			this.resultList.add(data);
		}
	}

	@Override
	public Object getResult(Object context, long timeout) throws Throwable {
		long start = System.currentTimeMillis();
		while (timeout > (System.currentTimeMillis() - start)) {
			if (count >= total) {
				break;
			}
			Thread.sleep(this.sleepTime);
		}
		// TODO 并发
		return this.resultList;
	}
}
