package org.xson.tangyuan.mr;

public interface MapReduceHander {

	public void merge(Object context, String service, Object result);

	public Object getResult(Object context, long timeout) throws Throwable;

}
