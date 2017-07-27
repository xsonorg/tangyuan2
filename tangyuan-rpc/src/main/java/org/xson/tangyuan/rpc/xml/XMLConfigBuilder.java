package org.xson.tangyuan.rpc.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.rpc.RpcContainer;
import org.xson.tangyuan.rpc.RpcProxy;
import org.xson.tangyuan.rpc.client.AbstractClientRpc;
import org.xson.tangyuan.rpc.client.MixedRpcClient;
import org.xson.tangyuan.rpc.xml.RpcClientVo.ClientUseType;
import org.xson.tangyuan.util.Resources;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.xml.XPathParser;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlExtendBuilder;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XMLConfigBuilder implements XmlExtendBuilder {

	private Log						log			= LogFactory.getLog(getClass());
	private XPathParser				xPathParser	= null;
	private XmlNodeWrapper			root		= null;

	// private String pigeonResource = null;
	// private boolean usePigeonServer = false;
	// private boolean usePigeonClient = false;

	private Map<String, Integer>	clientIdMap	= new HashMap<String, Integer>();

	@Override
	public void parse(XmlContext xmlContext, String resource) throws Throwable {
		log.info("*** Start parsing: " + resource);
		InputStream inputStream = Resources.getResourceAsStream(resource);
		this.xPathParser = new XPathParser(inputStream);
		root = xPathParser.evalNode("/rpc-component");
		parseNode();
	}

	public void parseNode() throws Throwable {
		buildConfigNodes(this.root.evalNodes("config-property"));
		List<RpcClientVo> clientVoList = buildClientNodes(this.root.evalNodes("client"));
		List<RemoteNodeVo> remoteVoList = buildRemoteNodes(this.root.evalNodes("remote-node"));

		Map<String, RemoteNodeVo> remoteNodeMap = new HashMap<String, RemoteNodeVo>();

		String defaultClientId = null;
		// clientId->rpc impl
		Map<String, AbstractClientRpc> rpcImplMap = new HashMap<String, AbstractClientRpc>();
		for (RpcClientVo rcVo : clientVoList) {
			rpcImplMap.put(rcVo.getId(), rcVo.create());
			log.info("init rpc client: " + rcVo.getId());
			defaultClientId = rcVo.getId();
		}

		// domain->rpc impl
		Map<String, AbstractClientRpc> rpcClientMap = new HashMap<String, AbstractClientRpc>();
		for (RemoteNodeVo rnVo : remoteVoList) {
			rpcClientMap.put(rnVo.getDomain(), rpcImplMap.get(rnVo.getClient()));
			remoteNodeMap.put(rnVo.getId(), rnVo);
		}

		// 创建客户端
		// if (1 == rpcImplMap.size() && 0 == remoteVoList.size()) {
		// RpcProxy.setRpc(new MixedRpcClient(rpcImplMap.get(defaultClientId)));
		// } else if (rpcClientMap.size() > 0) {
		// RpcProxy.setRpc(new MixedRpcClient(rpcClientMap, remoteNodeMap));
		// }

		if (1 == rpcImplMap.size()) {
			RpcProxy.setRpc(new MixedRpcClient(rpcImplMap.get(defaultClientId)));
		} else if (rpcClientMap.size() > 0) {
			RpcProxy.setRpc(new MixedRpcClient(rpcClientMap, remoteNodeMap));
		}
	}

	private void buildConfigNodes(List<XmlNodeWrapper> contexts) {
		Map<String, String> configMap = new HashMap<String, String>();
		for (XmlNodeWrapper context : contexts) {
			String name = StringUtils.trim(context.getStringAttribute("name"));
			String value = StringUtils.trim(context.getStringAttribute("value"));
			if (null == name || null == value) {
				throw new XmlParseException("<config-property> missing name or value");
			}
			configMap.put(name.toUpperCase(), value);
		}
		if (configMap.size() > 0) {
			RpcContainer.getInstance().config(configMap);
		}
	}

	private List<RpcClientVo> buildClientNodes(List<XmlNodeWrapper> contexts) {
		List<RpcClientVo> list = new ArrayList<RpcClientVo>();
		for (XmlNodeWrapper context : contexts) {
			String id = StringUtils.trim(context.getStringAttribute("id"));
			String _use = StringUtils.trim(context.getStringAttribute("use"));
			String schema = StringUtils.trim(context.getStringAttribute("schema"));
			if (clientIdMap.containsKey(id)) {
				throw new XmlParseException("Duplicate client: " + id);
			}
			clientIdMap.put(id, 1);
			ClientUseType use = getClientUseType(_use);
			if (null == use) {
				use = ClientUseType.HTTP_CLIENT;
			}
			if (null == schema || "".equals(schema)) {
				schema = "http";
			}
			RpcClientVo vo = new RpcClientVo(id, use, schema);
			list.add(vo);
		}
		return list;
	}

	private List<RemoteNodeVo> buildRemoteNodes(List<XmlNodeWrapper> contexts) {
		Map<String, Integer> remoteIdMap = new HashMap<String, Integer>();
		List<RemoteNodeVo> list = new ArrayList<RemoteNodeVo>();
		for (XmlNodeWrapper context : contexts) {
			// remote-node id="service" domain="newspaper.gatherlife.service" client=""
			String id = StringUtils.trim(context.getStringAttribute("id"));
			String domain = StringUtils.trim(context.getStringAttribute("domain"));
			String client = StringUtils.trim(context.getStringAttribute("client"));
			if (remoteIdMap.containsKey(id)) {
				throw new XmlParseException("Duplicate remote-node: " + id);
			}
			remoteIdMap.put(id, 1);
			testStringEmpty(id, "the id attribute in <remote-node> node is not empty.");
			testStringEmpty(domain, "the domain attribute in <remote-node> node  is not empty. remote-node.id: " + id);
			testStringEmpty(client, "the client attribute in <remote-node> node is not empty. remote-node.id: " + id);
			if (!clientIdMap.containsKey(client)) {
				throw new XmlParseException("the client attribute in <remote-node> node is not invalid. remote-node.id: " + id);
			}
			RemoteNodeVo vo = new RemoteNodeVo(id, domain, client);
			list.add(vo);
		}
		return list;
	}

	private void testStringEmpty(String str, String message) {
		if (null == str || 0 == str.length()) {
			throw new XmlParseException(message);
		}
	}

	private ClientUseType getClientUseType(String use) {
		if ("PIGEON".equalsIgnoreCase(use)) {
			return ClientUseType.PIGEON;
		} else if ("HTTP_CLIENT".equalsIgnoreCase(use)) {
			return ClientUseType.HTTP_CLIENT;
		}
		return null;
	}

}
