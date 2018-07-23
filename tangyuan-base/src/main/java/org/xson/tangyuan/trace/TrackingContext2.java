package org.xson.tangyuan.trace;

public class TrackingContext2 {

	private static ThreadLocal<Object> contextThreadLocal = new ThreadLocal<Object>();

	public static Object getThreadLocalParent() {
		Object parent = contextThreadLocal.get();
		if (null == parent) {
			cleanThreadLocalParent();
		}
		return parent;
	}

	public static void cleanThreadLocalParent() {
		contextThreadLocal.remove();
	}

	public static void setThreadLocalParent(Object parent) {
		contextThreadLocal.set(parent);
	}

	//	private Object										parent;
	//
	//	private Object										current;
	//
	//	public TrackingContext2(Object parent, Object current) {
	//		this.parent = parent;
	//		this.current = current;
	//	}
	//
	//	public Object getParent() {
	//		return parent;
	//	}
	//
	//	public Object getCurrent() {
	//		return current;
	//	}

}
