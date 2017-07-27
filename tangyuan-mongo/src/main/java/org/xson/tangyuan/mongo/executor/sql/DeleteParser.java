package org.xson.tangyuan.mongo.executor.sql;

public class DeleteParser extends SqlParser {

	// DELETE FROM 表名称 WHERE 列名称 = 值
	public DeleteVo parse(String sql, String ucSql) {
		DeleteVo deleteVo = new DeleteVo();
		int length = sql.length();
		// int wherePos = ucSql.indexOf(WHERE_MARK, DELETE_MARK.length());
		int wherePos = findWhere(ucSql, DELETE_MARK.length());
		if (-1 == wherePos) {
			// 没有where
			String table = sql.substring(DELETE_MARK.length(), length).trim();
			deleteVo.setTable(table);
		} else {
			String table = sql.substring(DELETE_MARK.length(), wherePos).trim();
			deleteVo.setTable(table);
			WhereCondition condition = parseSelectWhere(sql, wherePos + WHERE_MARK.length(), length);
			deleteVo.setCondition(condition);
		}
		deleteVo.check();
		return deleteVo;
	}

}
