package org.xson.tangyuan.mongo.executor;

import java.util.List;
import java.util.Map;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.executor.IServiceContext;
import org.xson.tangyuan.executor.IServiceExceptionInfo;
import org.xson.tangyuan.executor.ServiceException;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.mongo.xml.node.AbstractMongoNode;

public class MongoServiceContext implements IServiceContext {

	private StringBuilder	sqlBuilder		= null;

	/**
	 * 真正的数据源dsKey
	 */
	private String			realDsKey		= null;

	private MongoActuator	mongoActuator	= null;

	protected MongoServiceContext() {
		this.mongoActuator = new MongoActuator();
	}

	/**
	 * 重设执行环境
	 */
	public void resetExecEnv() {
		// 重设dskey
		this.realDsKey = null;
		// 重新设置sqlBuilder
		this.sqlBuilder = new StringBuilder();
	}

	public void addSql(String sql) {
		sqlBuilder.append(sql);
	}

	public String getSql() {
		return sqlBuilder.toString();
	}

	public void setDsKey(String realDsKey) {
		if (null == realDsKey || null == this.realDsKey) {
			this.realDsKey = realDsKey;
		} else if (!this.realDsKey.equals(realDsKey)) {
			throw new TangYuanException("暂不支持多dsKey:" + realDsKey);// 只有在分库分表的情况才会出现
		}
	}

	public List<Map<String, Object>> executeSelectSetListMap(AbstractMongoNode sqlNode, MappingVo resultMap, Integer fetchSize) throws Throwable {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		List<Map<String, Object>> result = mongoActuator.selectAllMap(dsKey, getSql(), resultMap, fetchSize);
		return result;
	}

	public List<XCO> executeSelectSetListXCO(AbstractMongoNode sqlNode, MappingVo resultMap, Integer fetchSize) throws Throwable {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		List<XCO> result = mongoActuator.selectAllXCO(dsKey, getSql(), resultMap, fetchSize);
		return result;
	}

	public Map<String, Object> executeSelectOneMap(AbstractMongoNode sqlNode, MappingVo resultMap, Integer fetchSize) throws Throwable {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		Map<String, Object> result = mongoActuator.selectOneMap(dsKey, getSql(), resultMap, fetchSize);
		return result;
	}

	public XCO executeSelectOneXCO(AbstractMongoNode sqlNode, MappingVo resultMap, Integer fetchSize) throws Throwable {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		XCO result = mongoActuator.selectOneXCO(dsKey, getSql(), resultMap, fetchSize);
		return result;
	}

	public Object executeSelectVar(AbstractMongoNode sqlNode) throws Throwable {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		Object result = mongoActuator.selectVar(dsKey, getSql());
		return result;
	}

	public int executeDelete(AbstractMongoNode sqlNode) throws Throwable {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		int result = mongoActuator.delete(dsKey, getSql());
		return result;
	}

	public int executeUpdate(AbstractMongoNode sqlNode) throws Throwable {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		int result = mongoActuator.update(dsKey, getSql());
		return result;
	}

	public Object executeInsert(AbstractMongoNode sqlNode) throws Throwable {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		Object result = mongoActuator.insert(dsKey, getSql());
		return result;
	}

	// public int executeInsert(AbstractMongoNode sqlNode) throws Throwable {
	// String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
	// int result = mongoActuator.insert(dsKey, getSql());
	// return result;
	// }

	// public InsertReturn executeInsertReturn(AbstractMongoNode sqlNode) throws Throwable {
	// String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
	// InsertReturn result = mongoActuator.insertReturn(dsKey, getSql());
	// return result;
	// }

	@Override
	public void commit(boolean confirm) throws Throwable {
	}

	@Override
	public void rollback() {
	}

	@Override
	public boolean onException(IServiceExceptionInfo exceptionInfo) throws ServiceException {
		// 这里不能处理任务错误,统一上抛
		return false;
	}

}
