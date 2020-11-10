package org.xson.tangyuan.rpc.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.tangyuan.manager.conf.ResourceReloaderVo;
import org.xson.tangyuan.rpc.RpcComponent;
import org.xson.tangyuan.rpc.TangYuanRpcPlaceHolderHandler;
import org.xson.tangyuan.rpc.balance.BalanceManager;
import org.xson.tangyuan.rpc.client.AbstractRpcClient;
import org.xson.tangyuan.rpc.client.MixedRpcClient;
import org.xson.tangyuan.rpc.client.NoRpcClient;
import org.xson.tangyuan.rpc.host.RemoteHostManager;
import org.xson.tangyuan.rpc.xml.vo.RemoteNodeVo;
import org.xson.tangyuan.rpc.xml.vo.RpcClientVo;
import org.xson.tangyuan.xml.DefaultXmlComponentBuilder;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlGlobalContext;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XmlRpcComponentBuilder extends DefaultXmlComponentBuilder {

	private XmlRpcContext			componentContext			= null;

	private Map<String, Integer>	clientIdMap					= new HashMap<String, Integer>();
	/** Placeholder Remote Node List */
	private Map<String, Integer>	placeHolderRemoteNodeMap	= new HashMap<String, Integer>();
	private RemoteHostManager		remoteHostManager			= null;
	private String					defaultClientRpcId			= null;

	@Override
	public void parse(XmlContext xmlContext, String resource) throws Throwable {
		log.info(lang("xml.start.parsing", resource));

		this.componentContext = (XmlRpcContext) xmlContext;
		this.globalContext = this.componentContext.getXmlContext();

		this.init(resource, "rpc-component", true);
		this.configurationElement();
		this.clean();
	}

	@Override
	protected void clean() {
		super.clean();

		this.componentContext = null;
		this.clientIdMap = null;
		this.placeHolderRemoteNodeMap = null;
		this.remoteHostManager = null;
		this.defaultClientRpcId = null;
	}

	private void configurationElement() throws Throwable {
		buildConfigNode(this.root.evalNodes("config-property"), RpcComponent.getInstance());

		buildBalanceNode(getMostOneNode(this.root, "balance", lang("xml.tag.mostone", "balance")));
		buildRemoteHostNode(getMostOneNode(this.root, "remote-host", lang("xml.tag.mostone", "remote-host")));

		List<RpcClientVo> clientVoList = buildClientNode(this.root.evalNodes("client"));
		List<RemoteNodeVo> remoteVoList = buildRemoteNode(this.root.evalNodes("remote-node"));

		initAll(clientVoList, remoteVoList);
	}

	private void setDefaultClientRpcId(String defaultClientRpcId, String tagName) {
		if (null != this.defaultClientRpcId) {
			throw new XmlParseException(lang("xml.tag.mostone.default.resource", tagName, this.resource));
		}
		this.defaultClientRpcId = defaultClientRpcId;
	}

	private void initAll(List<RpcClientVo> clientVoList, List<RemoteNodeVo> remoteVoList) throws Throwable {

		// 占位remote特殊处理
		if (this.placeHolderRemoteNodeMap.size() > 0) {
			this.globalContext.setRpcPlaceHolderHandler(new TangYuanRpcPlaceHolderHandler(this.placeHolderRemoteNodeMap));
			// RpcComponent.getInstance().setRpcPlaceHolderHandler(new TangYuanRpcPlaceHolderHandler(this.placeHolderRemoteNodeMap));
		}
		if (0 == clientVoList.size()) {
			RpcComponent.getInstance().setRpcClient(new NoRpcClient());
			return;// do nothing
		}
		if (0 == remoteVoList.size() && clientVoList.size() > 1) {
			RpcComponent.getInstance().setRpcClient(new NoRpcClient());
			return;// do nothing
		}

		BalanceManager balanceManager = this.componentContext.getBalanceManager();
		String onlyClientRpcId = null;
		Map<String, RemoteNodeVo> remoteNodeMap = new HashMap<String, RemoteNodeVo>();

		// clientId->rpc impl
		Map<String, AbstractRpcClient> rpcImplMap = new HashMap<String, AbstractRpcClient>();
		for (RpcClientVo rcVo : clientVoList) {
			AbstractRpcClient rpc = rcVo.create(balanceManager, this.remoteHostManager);
			rpcImplMap.put(rcVo.getId(), rpc);
			onlyClientRpcId = rcVo.getId();
			// if (rcVo.isDefaultClient()) {
			// defaultClientRpc = rpc;
			// }
			// log.info("init rpc client: " + rcVo.getId());
			log.info(lang("add.tag", "client", rcVo.getId()));
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
		// 4. 支持默认客户端, 可兼容直接写域名不要配置remote-node

		AbstractRpcClient rpcClient = null;
		if (1 == rpcImplMap.size() && 0 == remoteVoList.size()) {
			rpcClient = new MixedRpcClient(rpcClientMap, rpcImplMap.get(onlyClientRpcId));
		} else {
			// rpcClient = new MixedRpcClient(rpcClientMap, remoteNodeMap, defaultClientRpc);
			rpcClient = new MixedRpcClient(rpcClientMap, remoteNodeMap, rpcImplMap.get(defaultClientRpcId));
		}

		RpcComponent.getInstance().setRpcClient(rpcClient);
	}

	private List<RpcClientVo> buildClientNode(List<XmlNodeWrapper> contexts) {
		String tagName = "client";
		List<RpcClientVo> list = new ArrayList<RpcClientVo>();
		for (XmlNodeWrapper xNode : contexts) {

			String id = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String schema = getStringFromAttr(xNode, "schema");
			String resource = getStringFromAttr(xNode, "resource");
			String usi = getStringFromAttr(xNode, "usi", lang("xml.tag.attribute.empty", "usi", tagName, this.resource));
			boolean defaultClient = getBoolFromAttr(xNode, "default", false);

			if (clientIdMap.containsKey(id)) {
				throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
			}

			if (defaultClient) {
				setDefaultClientRpcId(id, tagName);
			}

			RpcClientVo vo = new RpcClientVo(id, schema, resource, usi, defaultClient);
			list.add(vo);
			clientIdMap.put(id, 1);
		}
		return list;
	}

	private List<RemoteNodeVo> buildRemoteNode(List<XmlNodeWrapper> contexts) {
		String tagName = "remote-node";
		Map<String, Integer> remoteIdMap = new HashMap<String, Integer>();
		List<RemoteNodeVo> list = new ArrayList<RemoteNodeVo>();
		for (XmlNodeWrapper xNode : contexts) {

			String id = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String domain = getStringFromAttr(xNode, "domain", lang("xml.tag.attribute.empty", "domain", tagName, this.resource));
			String client = getStringFromAttr(xNode, "client");

			if (remoteIdMap.containsKey(id)) {
				throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
			}

			if ("@".equals(domain)) {
				placeHolderRemoteNodeMap.put(id, 1);
			} else {
				if (!clientIdMap.containsKey(client)) {
					throw new XmlParseException(lang("xml.tag.attribute.invalid", "client", tagName, this.resource));
				}
				RemoteNodeVo vo = new RemoteNodeVo(id, domain, client);
				list.add(vo);
			}
			remoteIdMap.put(id, 1);
			log.info(lang("add.tag", tagName, id));
		}
		return list;
	}

	private void buildBalanceNode(XmlNodeWrapper xNode) throws Throwable {
		if (null == xNode) {
			return;
		}
		String tagName = "balance";
		String resource = getStringFromAttr(xNode, "resource", lang("xml.tag.attribute.empty", "resource", tagName, this.resource));

		XmlRpcBalanceBuilder builder = new XmlRpcBalanceBuilder();
		builder.parse(this.componentContext, resource);
	}

	private void buildRemoteHostNode(XmlNodeWrapper xNode) throws Throwable {
		if (null == xNode) {
			return;
		}
		String tagName = "remote-host";
		String resource = getStringFromAttr(xNode, "resource", lang("xml.tag.attribute.empty", "resource", tagName, this.resource));

		this.remoteHostManager = new RemoteHostManager();

		try {
			this.remoteHostManager.init(resource);
			XmlGlobalContext.addReloaderVo(new ResourceReloaderVo(resource, this.remoteHostManager));
			log.info(lang("xml.tag.resource.load", tagName, resource));
		} catch (Throwable e) {
			this.remoteHostManager = null;
			log.error(lang("xml.tag.resource.load.failed", tagName, resource));
		}
	}

}
