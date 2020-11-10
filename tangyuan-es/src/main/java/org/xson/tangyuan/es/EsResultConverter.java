package org.xson.tangyuan.es;

public interface EsResultConverter {

	Object convert(String json) throws Throwable;

	Object convertOnError(String json, int httpState) throws Throwable;
}
