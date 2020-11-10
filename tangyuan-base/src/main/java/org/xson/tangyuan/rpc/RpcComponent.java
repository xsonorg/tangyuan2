package org.xson.tangyuan.rpc;

import java.util.Map;

import org.xson.tangyuan.ComponentVo;
import org.xson.tangyuan.TangYuanComponent;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.Version;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.manager.TangYuanState.ComponentState;
import org.xson.tangyuan.rpc.client.AbstractRpcClient;
import org.xson.tangyuan.rpc.xml.XmlRpcComponentBuilder;
import org.xson.tangyuan.rpc.xml.XmlRpcContext;
import org.xson.tangyuan.service.context.DefaultServiceContextFactory;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

/**
 * 先启动, 后关闭
 */
public class RpcComponent implements TangYuanComponent {

	private static RpcComponent     instance     = new RpcComponent();

	private Log                     log          = LogFactory.getLog(getClass());

	private int                     errorCode    = -1;
	private String                  errorMessage = "RPC调用异常";

	private AbstractRpcClient       rpcClient    = null;

	private volatile ComponentState state        = ComponentState.UNINITIALIZED;

	static {
		TangYuanContainer.getInstance().registerContextFactory(TangYuanServiceType.PRCPROXY, new DefaultServiceContextFactory());
		TangYuanContainer.getInstance().registerComponent(new ComponentVo(instance, "rpc"));
	}

	private RpcComponent() {
	}

	public static RpcComponent getInstance() {
		return instance;
	}

	@Override
	public void config(Map<String, String> properties) {
		if (properties.containsKey("errorCode".toUpperCase())) {
			errorCode = Integer.parseInt(properties.get("errorCode".toUpperCase()));
		}
		if (properties.containsKey("errorMessage".toUpperCase())) {
			errorMessage = properties.get("errorMessage".toUpperCase());
		}
		log.info(TangYuanLang.get("config.property.load"), "rpc-component");
	}

	public int getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setRpcClient(AbstractRpcClient rpcClient) {
		this.rpcClient = rpcClient;
	}

	public AbstractRpcClient getRpcClient() {
		return rpcClient;
	}

	//	public void setRpcPlaceHolderHandler(RpcPlaceHolderHandler rpcPlaceHolderHandler) {
	//		this.rpcPlaceHolderHandler = rpcPlaceHolderHandler;
	//	}
	//	public RpcPlaceHolderHandler getRpcPlaceHolderHandler() {
	//		return rpcPlaceHolderHandler;
	//	}

	public boolean isRunning() {
		return ComponentState.RUNNING == this.state;
	}

	@Override
	public void start(String resource) throws Throwable {
		//		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		//		log.info(TangYuanLang.get("component.starting"), "rpc", Version.getVersion());

		log.info(TangYuanLang.get("component.dividing.line"));
		log.info(TangYuanLang.get("component.starting"), "rpc", Version.getVersion());

		this.state = ComponentState.INITIALIZING;

		XmlRpcContext componentContext = new XmlRpcContext();
		componentContext.setXmlContext(TangYuanContainer.getInstance().getXmlGlobalContext());

		XmlRpcComponentBuilder xmlConfigBuilder = new XmlRpcComponentBuilder();
		xmlConfigBuilder.parse(componentContext, resource);
		componentContext.clean();

		this.state = ComponentState.RUNNING;

		log.info(TangYuanLang.get("component.starting.successfully"), "rpc");

	}

	@Override
	public void stop(long waitingTime, boolean asyn) {
		this.state = ComponentState.CLOSED;
		log.info(TangYuanLang.get("component.stopping.successfully"), "rpc");
	}

	//	private RpcPlaceHolderHandler   rpcPlaceHolderHandler = null;
	//	private AbstractRpcClient rpcClient = null;
	//	public void setRpcClient(AbstractRpcClient rpcClient) {
	//		this.rpcClient = rpcClient;
	//	}
}
