package org.xson.tangyuan.hive.transaction;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class XTransactionStatus {

	protected String					name			= null;

	// group.xxxx
	protected String					dsKey			= null;

	protected XConnection				xConnection		= null;

	// 多数据源的时候存在, 优化的时候考虑使用简单MAP:LinkedHashMap
	protected Map<String, XConnection>	connMap			= null;

	protected XTransactionDefinition	definition		= null;

	// 是否使用单数据源
	protected boolean					singleDs		= true;

	// 是否初始化, 适用于sqlService
	protected boolean					initialization	= true;

	public boolean existXConnection(String dsKey) {
		if (null == getConnection(dsKey)) {
			return false;
		}
		return true;
	}

	protected void clearSingleSource() {
		this.dsKey = null;
		this.xConnection = null;
	}

	protected void addXConnection(String newDsKey, XConnection newXConnection) {
		if (null == this.xConnection && null == this.connMap) {
			this.dsKey = newDsKey;
			this.xConnection = newXConnection;
			return;
		}
		if (null == connMap) {
			connMap = new HashMap<String, XConnection>();
			connMap.put(this.dsKey, this.xConnection);
			clearSingleSource();
			singleDs = false;
		}
		connMap.put(newDsKey, newXConnection);
	}

	public Connection getConnection(String dsKey) {
		if (null == xConnection && null == connMap) {
			return null;
		}
		if (null != xConnection) {
			if (dsKey.equalsIgnoreCase(this.dsKey)) {
				return this.xConnection.getConnection();
			}
			return null;
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

}