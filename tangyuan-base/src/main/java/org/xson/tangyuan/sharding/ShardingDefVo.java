package org.xson.tangyuan.sharding;

import org.xson.tangyuan.ognl.vars.Variable;

public class ShardingDefVo {

	public enum ShardingMode {
		RANGE, HASH, MOD, RANDOM
	}

	private String			table;
	private String			dataSource;
	private ShardingMode	mode;
	private int				dbCount;
	private int				tableCount;
	private int				tableCapacity;
	private Variable[]		keywords;
	private boolean			tableNameIndexIncrement;	// 表名称索引是否递增
	private ShardingHandler	handler;					// impl="mySharding"
	private int				dataSourceCount;			// 物理数据源数量
	private boolean			dataSourceGroup;			// 是否是数据源组
	private boolean			requireKeyword;				// 是否一定需要关键字

	// 这里不使用默认值, 默认值应该在sharding.table中定义
	// 默认的数据源: 选择失败的时候使用
	private String			defaultDataSource;

	public ShardingDefVo(String table, String dataSource, ShardingMode mode, int dbCount, int tableCount, int tableCapacity, Variable[] keywords,
			boolean tableNameIndexIncrement, ShardingHandler handler, int dataSourceCount, boolean dataSourceGroup, boolean requireKeyword,
			String defaultDataSource) {
		this.table = table;
		this.dataSource = dataSource;
		this.mode = mode;
		this.dbCount = dbCount;
		this.tableCount = tableCount;
		this.tableCapacity = tableCapacity;
		this.keywords = keywords;
		this.tableNameIndexIncrement = tableNameIndexIncrement;
		this.handler = handler;
		this.dataSourceCount = dataSourceCount;
		this.dataSourceGroup = dataSourceGroup;
		this.requireKeyword = requireKeyword;
		this.defaultDataSource = defaultDataSource;
	}

	protected String getTable() {
		return table;
	}

	protected String getDataSource() {
		return dataSource;
	}

	protected ShardingMode getMode() {
		return mode;
	}

	protected int getDbCount() {
		return dbCount;
	}

	public int getTableCount() {
		return tableCount;
	}

	public int getTableCapacity() {
		return tableCapacity;
	}

	public Variable[] getKeywords() {
		return keywords;
	}

	protected ShardingHandler getHandler() {
		return handler;
	}

	public boolean isTableNameIndexIncrement() {
		return tableNameIndexIncrement;
	}

	public String getDefaultDataSource() {
		return defaultDataSource;
	}

	public int getDataSourceCount() {
		return dataSourceCount;
	}

	public boolean isDataSourceGroup() {
		return dataSourceGroup;
	}

	public boolean isRequireKeyword() {
		return requireKeyword;
	}

	public ShardingResult getShardingResult(ShardingArgVo argVo, Object arg) {
		return this.handler.selectDataSourceAndTable(this, argVo, arg);
	}

}
