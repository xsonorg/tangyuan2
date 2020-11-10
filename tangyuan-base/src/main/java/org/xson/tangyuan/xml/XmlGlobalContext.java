package org.xson.tangyuan.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.tangyuan.aop.Aop;
import org.xson.tangyuan.manager.ManagerLauncherContext;
import org.xson.tangyuan.manager.conf.ResourceReloaderVo;
import org.xson.tangyuan.rpc.RpcPlaceHolderHandler;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class XmlGlobalContext extends DefaultXmlContext {

	// static
	private static List<ResourceReloaderVo>	reloaderVoList			= new ArrayList<ResourceReloaderVo>();
	private static ManagerLauncherContext	mlc						= null;

	// normal
	private Map<String, TangYuanNode>		integralRefMap			= new HashMap<String, TangYuanNode>();
	private Map<String, Integer>			integralServiceMap		= new HashMap<String, Integer>();
	private Map<String, Integer>			integralServiceNsMap	= new HashMap<String, Integer>();
	private Map<String, Integer>			integralServiceClassMap	= new HashMap<String, Integer>();

	// other
	private RpcPlaceHolderHandler			rpcPlaceHolderHandler	= null;
	private Aop								aop						= null;

	// static ============================================================

	public static void addReloaderVo(ResourceReloaderVo rrVo) {
		reloaderVoList.add(rrVo);
	}

	public static void setMlc(ManagerLauncherContext launcherContext) {
		if (null != mlc) {
			mlc = launcherContext;
		}
	}

	public static ManagerLauncherContext getMlc() {
		return mlc;
	}

	public static List<ResourceReloaderVo> getReloaderVoList() {
		return reloaderVoList;
	}

	public void cleanStatic() {
		// TODO
	}

	// normal ============================================================

	public Map<String, TangYuanNode> getIntegralRefMap() {
		return integralRefMap;
	}

	public Map<String, Integer> getIntegralServiceMap() {
		return integralServiceMap;
	}

	public Map<String, Integer> getIntegralServiceNsMap() {
		return integralServiceNsMap;
	}

	public Map<String, Integer> getIntegralServiceClassMap() {
		return integralServiceClassMap;
	}

	@Override
	public void clean() {
		this.integralServiceNsMap.clear();
		this.integralServiceNsMap = null;

		this.integralRefMap.clear();
		this.integralRefMap = null;

		this.integralServiceMap = null;
		this.integralServiceClassMap = null;

		this.rpcPlaceHolderHandler = null;
		this.aop = null;
	}

	public RpcPlaceHolderHandler getRpcPlaceHolderHandler() {
		return rpcPlaceHolderHandler;
	}

	public void setRpcPlaceHolderHandler(RpcPlaceHolderHandler rpcPlaceHolderHandler) {
		this.rpcPlaceHolderHandler = rpcPlaceHolderHandler;
	}

	public Aop getAop() {
		return aop;
	}

	public void setAop(Aop aop) {
		this.aop = aop;
	}

}
