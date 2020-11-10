package org.xson.tangyuan.rpc.xml;

import org.xson.tangyuan.rpc.balance.BalanceManager;
import org.xson.tangyuan.xml.DefaultXmlContext;
import org.xson.tangyuan.xml.XmlGlobalContext;

public class XmlRpcContext extends DefaultXmlContext {

	private XmlGlobalContext xmlContext     = null;
	private BalanceManager   balanceManager = null;

	@Override
	public void clean() {
		this.xmlContext = null;
		this.balanceManager = null;
	}

	public XmlGlobalContext getXmlContext() {
		return xmlContext;
	}

	public void setXmlContext(XmlGlobalContext xmlContext) {
		this.xmlContext = xmlContext;
	}

	public void setBalanceManager(BalanceManager balanceManager) {
		this.balanceManager = balanceManager;
	}

	public BalanceManager getBalanceManager() {
		return balanceManager;
	}
}
