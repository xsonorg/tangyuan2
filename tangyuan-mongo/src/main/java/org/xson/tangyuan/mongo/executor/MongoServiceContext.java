package org.xson.tangyuan.mongo.executor;

import java.util.List;
import java.util.Map;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.executor.DefaultServiceContext;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.mongo.executor.cmd.CommandActuator;
import org.xson.tangyuan.mongo.xml.node.AbstractMongoNode;

public class MongoServiceContext extends DefaultServiceContext {

	private StringBuilder	sqlBuilder		= null;

	/**
	 * 真正的数据源dsKey
	 */
	private String			realDsKey		= null;

	private MongoActuator	mongoActuator	= null;
	private CommandActuator	commandActuator	= null;

	protected MongoServiceContext() {
		this.mongoActuator = new MongoActuator();
		this.commandActuator = new CommandActuator();
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

	public List<Map<String, Object>> executeSelectSetListMap(AbstractMongoNode sqlNode, MappingVo resultMap, Integer fetchSize, Object arg)
			throws Throwable {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		List<Map<String, Object>> result = mongoActuator.selectAllMap(dsKey, getSql(), resultMap, fetchSize, arg);
		return result;
	}

	public List<XCO> executeSelectSetListXCO(AbstractMongoNode sqlNode, MappingVo resultMap, Integer fetchSize, Object arg) throws Throwable {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		List<XCO> result = mongoActuator.selectAllXCO(dsKey, getSql(), resultMap, fetchSize, arg);
		return result;
	}

	public Map<String, Object> executeSelectOneMap(AbstractMongoNode sqlNode, MappingVo resultMap, Integer fetchSize, Object arg) throws Throwable {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		Map<String, Object> result = mongoActuator.selectOneMap(dsKey, getSql(), resultMap, fetchSize, arg);
		return result;
	}

	public XCO executeSelectOneXCO(AbstractMongoNode sqlNode, MappingVo resultMap, Integer fetchSize, Object arg) throws Throwable {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		XCO result = mongoActuator.selectOneXCO(dsKey, getSql(), resultMap, fetchSize, arg);
		return result;
	}

	public Object executeSelectVar(AbstractMongoNode sqlNode, Object arg) throws Throwable {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		Object result = mongoActuator.selectVar(dsKey, getSql(), arg);
		return result;
	}

	public int executeDelete(AbstractMongoNode sqlNode, Object arg) throws Throwable {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		int result = mongoActuator.delete(dsKey, getSql(), arg);
		return result;
	}

	public int executeUpdate(AbstractMongoNode sqlNode, Object arg) throws Throwable {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		int result = mongoActuator.update(dsKey, getSql(), arg);
		return result;
	}

	public Object executeInsert(AbstractMongoNode sqlNode, Object arg) throws Throwable {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		Object result = mongoActuator.insert(dsKey, getSql(), arg);
		return result;
	}

	public Object executeCommand(AbstractMongoNode sqlNode, Object arg, Class<?> resultType, MappingVo resultMap) throws Throwable {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		Object result = commandActuator.execute(getSql(), dsKey, resultType, resultMap);
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

}
