package org.xson.tangyuan.rpc.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.tangyuan.rpc.balance.BalanceHandler;
import org.xson.tangyuan.rpc.balance.BalanceHostVo;
import org.xson.tangyuan.rpc.balance.BalanceManager;
import org.xson.tangyuan.rpc.balance.BalanceVo;
import org.xson.tangyuan.rpc.balance.BalanceVo.Strategy;
import org.xson.tangyuan.rpc.balance.RountBalanceHandler;
import org.xson.tangyuan.rpc.balance.WeightBalanceHandler;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.xml.DefaultXmlComponentBuilder;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XmlRpcBalanceBuilder extends DefaultXmlComponentBuilder {

	private XmlRpcContext componentContext = null;

	@Override
	protected void parse(XmlContext xmlContext, String resource) throws Throwable {
		log.info(lang("xml.start.parsing.type", "balance", resource));
		this.componentContext = (XmlRpcContext) xmlContext;
		this.globalContext = this.componentContext.getXmlContext();
		this.init(resource, "balances", true);
		this.configurationElement();
		this.clean();
	}

	@Override
	protected void clean() {
		super.clean();
	}

	private void configurationElement() throws Throwable {
		buildBalanceNode(this.root.evalNodes("balance"));
	}

	private void buildBalanceNode(List<XmlNodeWrapper> contexts) throws Throwable {
		String                 tagName    = "balance";
		int                    size       = contexts.size();
		// <balance domain="test.xxax.com" strategy="weight/round" impl="a.b.c.d">
		Map<String, BalanceVo> balanceMap = new HashMap<>();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode     = contexts.get(i);

			String         domain    = getStringFromAttr(xNode, "domain", lang("xml.tag.attribute.empty", "domain", tagName, this.resource));
			String         _strategy = getStringFromAttr(xNode, "strategy");
			String         className = getStringFromAttr(xNode, "impl");

			if (balanceMap.containsKey(domain)) {
				throw new XmlParseException(lang("xml.tag.repeated", domain, tagName, this.resource));
			}
			Strategy       strategy = getStrategy(_strategy);

			BalanceHandler handler  = null;
			if (null != className) {
				handler = getInstanceForName(className, BalanceHandler.class, lang("xml.class.impl.interface", className, BalanceHandler.class.getName()));
			}
			if (null == handler && Strategy.ROUND == strategy) {
				handler = new RountBalanceHandler();
			}
			if (null == handler && Strategy.WEIGHT == strategy) {
				handler = new WeightBalanceHandler();
			}
			if (null == handler) {
				throw XmlParseException.createLang("xml.tag.attribute.empty", "strategy|impl", tagName, this.resource);
			}

			List<BalanceHostVo> hostList = buildHostNode(xNode.evalNodes("host"));
			if (hostList.size() < 1) {
				throw XmlParseException.createLang("xml.tag.sub.miss", "host", tagName, this.resource);
			}
			BalanceVo bVo = new BalanceVo(strategy, handler, hostList);
			balanceMap.put(domain, bVo);

			//			log.info("add balance: " + domain);
			log.info(lang("add.tag", tagName, domain));
		}

		if (balanceMap.size() > 0) {
			BalanceManager balanceManager = new BalanceManager(balanceMap);
			this.componentContext.setBalanceManager(balanceManager);
		}
	}

	private List<BalanceHostVo> buildHostNode(List<XmlNodeWrapper> contexts) {
		String              tagName  = "balance.host";
		List<BalanceHostVo> hostList = new ArrayList<>();
		for (XmlNodeWrapper xNode : contexts) {
			String domain = getStringFromAttr(xNode, "domain", lang("xml.tag.attribute.empty", "domain", tagName, this.resource));
			int    weight = getIntFromAttr(xNode, "weight", 1);
			//			int           port   = getIntFromAttr(xNode, "weight", 0);
			int    port   = -1;
			int    pos    = domain.indexOf(":");
			if (pos > -1) {
				port = Integer.parseInt(domain.substring(pos + 1));
				domain = domain.substring(0, pos);
			}
			BalanceHostVo hostVo = new BalanceHostVo(domain, port, weight);
			hostList.add(hostVo);
		}
		return hostList;
	}

	private Strategy getStrategy(String content) {
		if (Strategy.ROUND.toString().equalsIgnoreCase(content)) {
			return Strategy.ROUND;
		} else if (Strategy.WEIGHT.toString().equalsIgnoreCase(content)) {
			return Strategy.WEIGHT;
		} else if (StringUtils.isEmpty(content)) {
			return null;
		}
		//		throw new TangYuanException(lang("unsupported.type.n", "strategy", content));
		throw XmlParseException.createLang("unsupported.type.n", "strategy", content);
	}

}