package org.xson.tangyuan.es.util;

public class EsHttpResultWrapper {

	private String content;

	private int    httpState;

	public EsHttpResultWrapper(String content, int httpState) {
		this.content = content;
		this.httpState = httpState;
	}

	public String getContent() {
		return content;
	}

	public int getHttpState() {
		return httpState;
	}
}
