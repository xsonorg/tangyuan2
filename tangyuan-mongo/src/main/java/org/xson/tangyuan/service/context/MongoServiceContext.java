package org.xson.tangyuan.service.context;

import java.util.List;
import java.util.Map;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.ext.LogExtUtil;
import org.xson.tangyuan.manager.TangYuanManager;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.mongo.xml.node.AbstractMongoNode;
import org.xson.tangyuan.service.mongo.MongoActuator;
import org.xson.tangyuan.service.mongo.cmd.CommandActuator;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

public class MongoServiceContext extends DefaultServiceContext {

	private static Log		log				= LogFactory.getLog(MongoServiceContext.class);

	private StringBuilder	sqlBuilder		= null;
	/** 真正的数据源dsKey */
	private String			realDsKey		= null;

	private MongoActuator	mongoActuator	= null;
	private CommandActuator	commandActuator	= null;

	private TangYuanManager	tangYuanManager	= null;

	protected MongoServiceContext() {
		this.mongoActuator = new MongoActuator();
		this.commandActuator = new CommandActuator();
		this.tangYuanManager = TangYuanManager.getInstance();
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

	//////// //// //// //// //// //// //// //// //// //// //// //// //// //// //// ////

	public List<Map<String, Object>> executeSelectSetListMap(AbstractMongoNode sqlNode, MappingVo resultMap, Object arg) throws Throwable {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		String sql = getSql();
		try {
			List<Map<String, Object>> result = mongoActuator.selectAllMap(dsKey, sql, resultMap, arg);
			printSql(sql);
			appendTracking(sql);
			return result;
		} catch (Throwable e) {
			printErrorSql(sql);
			throw e;
		}
	}

	public Map<String, Object> executeSelectOneMap(AbstractMongoNode sqlNode, MappingVo resultMap, Object arg) throws Throwable {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		String sql = getSql();
		try {
			Map<String, Object> result = mongoActuator.selectOneMap(dsKey, sql, resultMap, arg);
			printSql(sql);
			appendTracking(sql);
			return result;
		} catch (Throwable e) {
			printErrorSql(sql);
			throw e;
		}
	}

	//////////// //// //// //// //// //// //// //// //// //// //// //// //// //// ////

	public List<XCO> executeSelectSetListXCO(AbstractMongoNode sqlNode, MappingVo resultMap, Object arg) throws Throwable {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		String sql = getSql();
		try {
			List<XCO> result = mongoActuator.selectAllXCO(dsKey, sql, resultMap, arg);
			printSql(sql);
			appendTracking(sql);
			return result;
		} catch (Throwable e) {
			printErrorSql(sql);
			throw e;
		}
	}

	public XCO executeSelectOneXCO(AbstractMongoNode sqlNode, MappingVo resultMap, Object arg) throws Throwable {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		String sql = getSql();
		try {
			XCO result = mongoActuator.selectOneXCO(dsKey, sql, resultMap, arg);
			printSql(sql);
			appendTracking(sql);
			return result;
		} catch (Throwable e) {
			printErrorSql(sql);
			throw e;
		}
	}

	public Object executeSelectVar(AbstractMongoNode sqlNode, MappingVo resultMap, Object arg) throws Throwable {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		String sql = getSql();
		try {
			Object result = mongoActuator.selectVar(dsKey, sql, resultMap, arg);
			printSql(sql);
			appendTracking(sql);
			return result;
		} catch (Throwable e) {
			printErrorSql(sql);
			throw e;
		}
	}

	public int executeDelete(AbstractMongoNode sqlNode, Object arg) throws Throwable {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		String sql = getSql();
		try {
			int result = mongoActuator.delete(dsKey, sql, arg);
			printSql(sql);
			appendTracking(sql);
			return result;
		} catch (Throwable e) {
			printErrorSql(sql);
			throw e;
		}
	}

	public int executeUpdate(AbstractMongoNode sqlNode, Object arg) throws Throwable {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		String sql = getSql();
		try {
			int result = mongoActuator.update(dsKey, sql, arg);
			printSql(sql);
			appendTracking(sql);
			return result;
		} catch (Throwable e) {
			printErrorSql(sql);
			throw e;
		}
	}

	public Object executeInsert(AbstractMongoNode sqlNode, Object arg) throws Throwable {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		String sql = getSql();
		try {
			Object result = mongoActuator.insert(dsKey, sql, arg);
			printSql(sql);
			appendTracking(sql);
			return result;
		} catch (Throwable e) {
			printErrorSql(sql);
			throw e;
		}
	}

	public Object executeCommand(AbstractMongoNode sqlNode, Object arg, Class<?> resultType, MappingVo resultMap) throws Throwable {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		String sql = getSql();
		try {
			Object result = commandActuator.execute(sql, dsKey, resultType, resultMap, arg);
			printSql(sql);
			appendTracking(sql);
			return result;
		} catch (Throwable e) {
			printErrorSql(sql);
			throw e;
		}
	}

	private void appendTracking(String sql) {
		if (isTraceCommand()) {
			this.tangYuanManager.appendTrackingCommand(sql);
		}
	}

	public boolean isTraceCommand() {
		if (null != this.tangYuanManager) {
			this.tangYuanManager.isTrackingCommand(TangYuanServiceType.MONGO.getVal());
		}
		return false;
	}

	private void printSql(String sql) {
		if (log.isInfoEnabled() && LogExtUtil.isMongoSqlPrint()) {
			log.info(sql);
		}
	}

	private void printErrorSql(String sql) {
		if (LogExtUtil.isMongoErrorLogPrint()) {
			System.err.print("相关异常语句[MONGO]:");
			System.err.print("\n\n\n");
			System.err.print(sql);
			System.err.print("\n\n\n");
		}
	}

}
