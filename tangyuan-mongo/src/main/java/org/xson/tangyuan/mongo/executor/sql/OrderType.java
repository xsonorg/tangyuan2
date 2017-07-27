package org.xson.tangyuan.mongo.executor.sql;

public enum OrderType {

	ASC(1), DESC(-1);

	private int value;

	private OrderType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
