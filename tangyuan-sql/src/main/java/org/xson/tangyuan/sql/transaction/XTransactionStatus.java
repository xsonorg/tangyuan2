package org.xson.tangyuan.sql.transaction;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class XTransactionStatus {

	protected String					name				= null;

	// group.xxxx
	protected String					dsKey				= null;

	protected XConnection				xConnection			= null;

	// 多数据源的时候存在, 优化的时候考虑使用简单MAP:LinkedHashMap
	protected Map<String, XConnection>	connMap				= null;

	protected XTransactionDefinition	definition			= null;

	protected XTransactionStatus		suspendedResources	= null;

	// 是否是一个独立的事务 // 是一个独立的事务:REQUIRES_NEW,NOT_SUPPORTED
	protected boolean					newTransaction		= false;

	// 是否开启了事务
	protected boolean					hasTransaction		= false;

	// 是否使用单数据源
	protected boolean					singleDs			= true;

	// 是否初始化, 适用于sqlService
	protected boolean					initialization		= true;

	// 是否含有Savepoint
	protected boolean					hasSavepoint		= false;

	// protected boolean independent = false;

	protected XTransactionStatus findTxByName(String target) {
		if (target.equalsIgnoreCase(name)) {
			return this;
		}

		if (null != suspendedResources) {
			return suspendedResources.findTxByName(target);
		}

		return null;
	}

	protected void clearSingleSource() {
		this.dsKey = null;
		this.xConnection = null;
	}

	protected void addXConnection(String newDsKey, XConnection newXConnection) {
		if (null == connMap) {
			connMap = new HashMap<String, XConnection>();
			connMap.put(this.dsKey, this.xConnection);
			clearSingleSource();
			singleDs = false;
		}
		connMap.put(newDsKey, newXConnection);
	}

	protected XConnection getXConnectionFromMap(String dsKey) {
		if (null == connMap) {
			return null;
		}
		return connMap.get(dsKey);
	}

	public Connection getConnection(String dsKey) {
		if (null != xConnection) {
			return this.xConnection.getConnection();
		}
		XConnection _xConnection = connMap.get(dsKey);
		if (null != _xConnection) {
			return _xConnection.getConnection();
		}
		return null;
	}

	public XTransactionDefinition getDefinition() {
		return this.definition;
	}

	// public boolean transaction = false;
	// 首个事务是否初始化
	// public boolean init = false;
	// public XTransaction transaction = null;
	// // 这里还要考虑多个连接的情况,可在之前分析，有些或许需要运行时分析
	// public Connection conn = null;
	// // 多数据源的时候存在, 优化的时候考虑使用简单MAP:LinkedHashMap
	// public Map<String, Connection> connMap = null;
	// public boolean readOnly = false;
	// 当事务已经递交或者被回滚就标志着已完成
	// public boolean completed = false;
	// 是否已被标记为回滚，如果返回值为 true 则在commit 时会回滚该事务。
	// public boolean rollbackOnly = false;
	// public boolean debug;
	// public boolean newSynchronization;
	// public Map<String, XTransactionStatus> txMap = null;
	// 是否存在事务的引用,通过txUse标签
	// public boolean txUse = false;
}