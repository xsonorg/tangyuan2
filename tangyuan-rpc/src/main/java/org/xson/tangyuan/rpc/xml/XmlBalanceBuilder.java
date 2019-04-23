package org.xson.tangyuan.rpc.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.rpc.balance.BalanceHandler;
import org.xson.tangyuan.rpc.balance.BalanceHostVo;
import org.xson.tangyuan.rpc.balance.BalanceManager;
import org.xson.tangyuan.rpc.balance.BalanceVo;
import org.xson.tangyuan.rpc.balance.BalanceVo.Strategy;
import org.xson.tangyuan.rpc.balance.RountBalanceHandler;
import org.xson.tangyuan.rpc.balance.WeightBalanceHandler;
import org.xson.tangyuan.util.ClassUtils;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.xml.XPathParser;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XmlBalanceBuilder {

	private Log			log			= LogFactory.getLog(getClass());
	private XPathParser	xPathParser	= null;

	public XmlBalanceBuilder(InputStream inputStream) {
		this.xPathParser = new XPathParser(inputStream);
	}

	public void parse(XmlContext xmlContext) throws Throwable {
		configurationElement(xPathParser.evalNode("/balances"));
	}

	private void configurationElement(XmlNodeWrapper context) throws Throwable {
		buildBalanceNodes(context.evalNodes("balance"));
	}

	private void buildBalanceNodes(List<XmlNodeWrapper> contexts) throws Throwable {
		int size = contexts.size();
		// <balance domain="test.xxax.com" strategy="weight/round" impl="a.b.c.d">
		Map<String, BalanceVo> balanceMap = new HashMap<>();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);
			String domain = StringUtils.trim(xNode.getStringAttribute("domain"));
			String _strategy = StringUtils.trim(xNode.getStringAttribute("strategy"));
			String className = StringUtils.trim(xNode.getStringAttribute("impl"));
			if (balanceMap.containsKey(domain)) {
				throw new XmlParseException("Duplicate balance: " + domain);
			}
			Strategy strategy = getStrategy(_strategy);
			BalanceHandler handler = null;
			if (null != className) {
				Class<?> handlerClass = ClassUtils.forName(className);
				if (!BalanceHandler.class.isAssignableFrom(handlerClass)) {
					throw new XmlParseException("impl class not implement the BalanceHandler interface: " + className);
				}
				handler = (BalanceHandler) handlerClass.newInstance();
			}
			if (null == handler && Strategy.ROUND == strategy) {
				handler = new RountBalanceHandler();
			}
			if (null == handler && Strategy.WEIGHT == strategy) {
				handler = new WeightBalanceHandler();
			}
			if (null == handler) {
				throw new XmlParseException("in tag <balance>, missing attribute 'strategy' or 'impl'");
			}

			List<BalanceHostVo> hostList = buildHostNodes(xNode.evalNodes("host"));
			if (hostList.size() < 2) {
				throw new XmlParseException("<host> tag at least two.");
			}
			BalanceVo bVo = new BalanceVo(strategy, handler, hostList);

			balanceMap.put(domain, bVo);
			log.info("add balance: " + domain);
		}

		if (balanceMap.size() > 0) {
			BalanceManager.getInstance().setBalanceMap(balanceMap);
		}
	}

	private List<BalanceHostVo> buildHostNodes(List<XmlNodeWrapper> contexts) {
		List<BalanceHostVo> hostList = new ArrayList<>();
		for (XmlNodeWrapper context : contexts) {
			// <host domain="testa.xxax.com" weight="2" />
			String domain = StringUtils.trim(context.getStringAttribute("domain"));
			String _weight = StringUtils.trim(context.getStringAttribute("weight"));
			String _port = StringUtils.trim(context.getStringAttribute("port"));
			int weight = 1;
			if (!StringUtils.isEmpty(_weight)) {
				weight = Integer.parseInt(_weight);
			}
			int port = 0;
			if (!StringUtils.isEmpty(_port)) {
				port = Integer.parseInt(_port);
			}
			BalanceHostVo hostVo = new BalanceHostVo(domain, port, weight);
			hostList.add(hostVo);
		}
		return hostList;
	}

	private Strategy getStrategy(String _strategy) {
		if ("ROUND".equalsIgnoreCase(_strategy)) {
			return Strategy.ROUND;
		} else if ("WEIGHT".equalsIgnoreCase(_strategy)) {
			return Strategy.WEIGHT;
		} else if (null == _strategy || "".equalsIgnoreCase(_strategy)) {
			return null;
		}
		throw new TangYuanException("Unsupported strategy: " + _strategy);
	}

}
