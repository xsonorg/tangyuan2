package org.xson.tangyuan.sharding;

public class ShardingResult {

	private String	table;

	private String	dataSource;

	public ShardingResult(String table, String dataSource) {
		this.table = table;
		this.dataSource = dataSource;
	}

	public String getTable() {
		return table;
	}

	public String getDataSource() {
		return dataSource;
	}

}
