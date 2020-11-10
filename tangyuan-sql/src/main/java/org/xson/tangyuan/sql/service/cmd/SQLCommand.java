package org.xson.tangyuan.sql.service.cmd;

import java.util.List;

import org.xson.common.object.XCO;

public interface SQLCommand {

	public <T> T execute(String sqlURI, Object arg) throws Throwable;

	public List<XCO> selectSet(String sqlURI, XCO arg) throws Throwable;

	public XCO selectOne(String sqlURI, XCO arg) throws Throwable;

	public <T> T selectVar(String sqlURI, XCO arg) throws Throwable;

	public int update(String sqlURI, XCO arg) throws Throwable;

	public int delete(String sqlURI, XCO arg) throws Throwable;

	public <T> T insert(String sqlURI, XCO arg) throws Throwable;

}
