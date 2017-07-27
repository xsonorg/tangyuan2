package org.xson.tangyuan.sharding;

public interface ShardingHandler {

	// 自动选择数据库连接，并生成物理表名
	// 将{}内的内容替换成成对物理表的表名
	// 自动选择数据库连接，并生成物理表索引
	// 将{}内的内容替换成物理表的索引
	// 自动选择DataGroupId及数据库连接，不涉及物理表名

	// properties: 参数对象
	// public ShardingResult selectDataSourceAndTable(ShardingDefVo defVo, ShardingArgVo argVo, Map<String, Object> arg);

	public ShardingResult selectDataSourceAndTable(ShardingDefVo defVo, ShardingArgVo argVo, Object arg);

}
