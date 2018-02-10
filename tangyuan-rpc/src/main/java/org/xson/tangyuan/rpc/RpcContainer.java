package org.xson.tangyuan.rpc;

import java.util.Map;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.ComponentVo;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.executor.DefaultServiceContext;
import org.xson.tangyuan.executor.IServiceContext;
import org.xson.tangyuan.executor.ServiceContextFactory;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

public class RpcContainer {

	private static RpcContainer	instance		= new RpcContainer();

	private Log					log				= LogFactory.getLog(getClass());
	private int					errorCode		= -1;
	private String				errorMessage	= "RPC异常";

	static {
		TangYuanContainer.getInstance().registerContextFactory(TangYuanServiceType.PRCPROXY, new ServiceContextFactory() {
			@Override
			public IServiceContext create() {
				return new DefaultServiceContext();
			}
		});
		// rpc 30 50
		// TangYuanContainer.getInstance().registerComponent(new ComponentVo(instance, "rpc", 30, 50));
		// TangYuanContainer.getInstance().registerComponent(new ComponentVo(RpcClientComponent.getInstance(), "rpc-client", 30, 50));
		// TangYuanContainer.getInstance().registerComponent(new ComponentVo(RpcServerComponent.getInstance(), "rpc-server", 30, 50));

		TangYuanContainer.getInstance().registerComponent(new ComponentVo(RpcClientComponent.getInstance(), "rpc-client"));
		TangYuanContainer.getInstance().registerComponent(new ComponentVo(RpcServerComponent.getInstance(), "rpc-server"));
	}

	private RpcContainer() {
	}

	public static RpcContainer getInstance() {
		return instance;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	/** 设置配置文件 */
	public void config(Map<String, String> properties) {
		if (properties.containsKey("errorCode".toUpperCase())) {
			errorCode = Integer.parseInt(properties.get("errorCode".toUpperCase()));
		}
		if (properties.containsKey("errorMessage".toUpperCase())) {
			errorMessage = properties.get("errorMessage".toUpperCase());
		}
		// if (properties.containsKey("httpSchema".toUpperCase())) {
		// httpSchema = properties.get("httpSchema".toUpperCase());
		// }
		// if (properties.containsKey("pigeonSchema".toUpperCase())) {
		// pigeonSchema = properties.get("pigeonSchema".toUpperCase());
		// }
		log.info("config setting success...");
	}

}
