package org.xson.tangyuan.log;

public interface LogLoader {

	//	public void load(String context) throws Throwable;
	//	public void reload(String context) throws Throwable;

	public void load(String resource) throws Throwable;

	public void reload(String resource) throws Throwable;

}
