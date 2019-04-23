package org.xson.tangyuan.rpc.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.rpc.RpcClientComponent;
import org.xson.tangyuan.rpc.RpcContainer;
import org.xson.tangyuan.rpc.RpcProxy;
import org.xson.tangyuan.rpc.TangYuanRpcPlaceHolderHandler;
import org.xson.tangyuan.rpc.client.AbstractRpcClient;
import org.xson.tangyuan.rpc.client.MixedRpcClient;
import org.xson.tangyuan.rpc.xml.RpcClientVo.ClientUseType;
import org.xson.tangyuan.util.ResourceManager;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.util.TangYuanAssert;
import org.xson.tangyuan.xml.XPathParser;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlExtendBuilder;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XMLConfigBuilder implements XmlExtendBuilder {

	private Log						log							= LogFactory.getLog(getClass());
	private XPathParser				xPathParser					= null;
	private XmlNodeWrapper			root						= null;

	// private String pigeonResource = null;
	// private boolean usePigeonServer = false;
	// private boolean usePigeonClient = false;

	private Map<String, Integer>	clientIdMap					= new HashMap<String, Integer>();

	// Placeholder Remote Node List
	private Map<String, Integer>	placeHolderRemoteNodeMap	= new HashMap<String, Integer>();

	@Override
	public void parse(XmlContext xmlContext, String resource) throws Throwable {
		log.info("*** Start parsing: " + resource);
		InputStream inputStream = ResourceManager.getInputStream(resource, true);
		this.xPathParser = new XPathParser(inputStream);
		root = xPathParser.evalNode("/rpc-component");
		parseNode();
		inputStream.close();
	}

	public void parseNode() throws Throwable {
		buildConfigNodes(this.root.evalNodes("config-property"));
		buildBalanceNodes(this.root.evalNodes("balance"));

		List<RpcClientVo> clientVoList = buildClientNodes(this.root.evalNodes("client"));
		List<RemoteNodeVo> remoteVoList = buildRemoteNodes(this.root.evalNodes("remote-node"));

		// 占位remote特殊处理
		if (this.placeHolderRemoteNodeMap.size() > 0) {
			RpcProxy.setPlaceHolderHandler(new TangYuanRpcPlaceHolderHandler(this.placeHolderRemoteNodeMap));
		}
		this.placeHolderRemoteNodeMap = null;

		if (0 == clientVoList.size()) {
			return;// do nothing
		}
		if (0 == remoteVoList.size() && clientVoList.size() > 1) {
			return;// do nothing
		}

		Map<String, RemoteNodeVo> remoteNodeMap = new HashMap<String, RemoteNodeVo>();

		String defaultClientId = null;
		// clientId->rpc impl
		Map<String, AbstractRpcClient> rpcImplMap = new HashMap<String, AbstractRpcClient>();
		for (RpcClientVo rcVo : clientVoList) {
			rpcImplMap.put(rcVo.getId(), rcVo.create());
			log.info("init rpc client: " + rcVo.getId());
			defaultClientId = rcVo.getId();
		}

		// domain->rpc impl
		Map<String, AbstractRpcClient> rpcClientMap = new HashMap<String, AbstractRpcClient>();
		for (RemoteNodeVo rnVo : remoteVoList) {
			rpcClientMap.put(rnVo.getDomain(), rpcImplMap.get(rnVo.getClient()));
			remoteNodeMap.put(rnVo.getId(), rnVo);
		}

		// 1. 1个客户端, n个remote
		// 2. 1个客户端, 0个remote(隐含支持所有)
		// 3. m个客户端, n个remote

		// 创建客户端, 非占位节点的
		// if (1 == rpcImplMap.size() && 0 == remoteVoList.size()) {
		// RpcProxy.setRpc(new MixedRpcClient(rpcImplMap.get(defaultClientId)));
		// } else {
		// RpcProxy.setRpc(new MixedRpcClient(rpcClientMap, remoteNodeMap));
		// }

		AbstractRpcClient rpcClient = null;
		if (1 == rpcImplMap.size() && 0 == remoteVoList.size()) {
			rpcClient = new MixedRpcClient(rpcImplMap.get(defaultClientId));
		} else {
			rpcClient = new MixedRpcClient(rpcClientMap, remoteNodeMap);
		}
		RpcProxy.setRpc(rpcClient);
		RpcClientComponent.getInstance().setRpcClient(rpcClient);
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

	private void buildBalanceNodes(List<XmlNodeWrapper> contexts) throws Throwable {
		int size = contexts.size();
		if (size == 0) {
			return;
		}
		if (size > 1) {
			throw new XmlParseException("<balance> tag can only be configured at most one.");
		}

		XmlNodeWrapper context = contexts.get(0);
		String resource = StringUtils.trim(context.getStringAttribute("resource"));
		TangYuanAssert.stringEmpty(resource, "in tag <balance>, missing attribute 'resource'");
		InputStream inputStream = ResourceManager.getInputStream(resource, true);

		XmlBalanceBuilder balanceBuilder = new XmlBalanceBuilder(inputStream);
		balanceBuilder.parse(null);
	}

	private List<RpcClientVo> buildClientNodes(List<XmlNodeWrapper> contexts) {
		List<RpcClientVo> list = new ArrayList<RpcClientVo>();
		for (XmlNodeWrapper context : contexts) {
			String id = StringUtils.trim(context.getStringAttribute("id"));
			String _use = StringUtils.trim(context.getStringAttribute("use"));
			String schema = StringUtils.trim(context.getStringAttribute("schema"));
			String resource = StringUtils.trim(context.getStringAttribute("resource"));
			String usi = StringUtils.trim(context.getStringAttribute("usi"));

			if (clientIdMap.containsKey(id)) {
				throw new XmlParseException("Duplicate client: " + id);
			}
			clientIdMap.put(id, 1);
			ClientUseType use = getClientUseType(_use);
			if (null == use) {
				use = ClientUseType.HTTP_CLIENT;
			}
			if ((ClientUseType.HTTP_CLIENT == use) && (null == schema || "".equals(schema))) {
				schema = "http";
			}

			if (ClientUseType.HTTP_CLIENT == use) {
				TangYuanAssert.stringEmpty(usi, "the 'usi' attribute cannot be empty.");
			}

			RpcClientVo vo = new RpcClientVo(id, use, schema, resource, usi);
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

			// <remote-node id="serviceC" domain="@"/>
			if ("@".equals(domain)) {
				// RemoteNodeVo vo = new RemoteNodeVo(id, domain, client);
				// phRemoteNodeList.add(vo);
				placeHolderRemoteNodeMap.put(id, 1);
				log.info("add placeholder remote-node: " + id);
			} else {
				testStringEmpty(client, "the client attribute in <remote-node> node is not empty. remote-node.id: " + id);
				if (!clientIdMap.containsKey(client)) {
					throw new XmlParseException("the client attribute in <remote-node> node is not invalid. remote-node.id: " + id);
				}
				RemoteNodeVo vo = new RemoteNodeVo(id, domain, client);
				list.add(vo);
			}
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
