package org.xson.tangyuan.sharding;

import org.xson.tangyuan.ognl.vars.Variable;

public class ShardingArgVo {

	// {X:xxx} sql-text {DT:tbTable} 自动选择数据库连接，并生成物理表名
	// select * from {DT:tbUser}
	// select * from {DT:tbUser,a,b,c}
	// {T:tbTable} 将{}内的内容替换成成对物理表的表名
	// select * from {T:tbUser}
	// select * from {T:tbUser,a,b,c}
	// {DI:tbTable} 自动选择数据库连接，并生成物理表索引
	// select * from tbUser{DI:tbUser}
	// select * from tbUser{DI:tbUser,a,b,c}
	// {I:tbTable} 将{}内的内容替换成物理表的索引
	// select * from tbUser{I:tbUser}
	// select * from tbUser{I:tbUser,a,b,c}
	// {D:tbTable} 自动选择DataGroupId及数据库连接，不涉及物理表名
	// select * from tbUser {D:tbUser}
	// select * from tbUser {D:tbUser,a,b,c}

	public enum ShardingTemplate {
		/**
		 * {DT:tbTable} 自动选择数据库连接，并生成物理表名
		 */
		DT,
		/**
		 * {T:tbTable} 将{}内的内容替换成成对物理表的表名
		 */
		T,
		/**
		 * {DI:tbTable} 自动选择数据库连接，并生成物理表索引
		 */
		DI,
		/**
		 * {I:tbTable} 将{}内的内容替换成物理表的索引
		 */
		I,
		/**
		 * {D:tbTable} 自动选择DataGroupId及数据库连接，不涉及物理表名
		 */
		D
	}

	private String				table;

	private ShardingTemplate	template;

	// 分库分表关键字集合, 选择可为空
	private Variable[]			keywords;

	private ShardingDefVo		shardingDef;

	public ShardingArgVo(String table, ShardingTemplate template, Variable[] keywords, ShardingDefVo shardingDef) {
		this.table = table;
		this.template = template;
		this.keywords = keywords;
		this.shardingDef = shardingDef;
	}

	public String getTable() {
		return table;
	}

	public Variable[] getKeywords() {
		return keywords;
	}

	public ShardingTemplate getTemplate() {
		return template;
	}

	public ShardingDefVo getShardingDef() {
		return shardingDef;
	}

	public ShardingResult getShardingResult(Object arg) {
		return shardingDef.getShardingResult(this, arg);
	}

}
