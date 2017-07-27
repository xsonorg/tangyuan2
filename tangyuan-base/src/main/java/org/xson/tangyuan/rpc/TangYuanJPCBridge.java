package org.xson.tangyuan.rpc;

import java.net.URI;

import org.xson.common.object.XCO;
import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.zongzi.JPCBridge;

public class TangYuanJPCBridge implements JPCBridge {

	private Log			log		= LogFactory.getLog(getClass());

	private JPCBridge	hook	= null;

	@Override
	public Object call(String path, Object arg) {
		log.info("client request to: " + path);
		log.info("client request args: " + arg);
		XCO result = null;
		// try {
		// result = RpcUtil.doXcoRpcRquest(path, (XCO) arg);
		// } catch (Throwable e) {
		// result = RpcUtil.getExceptionResult(e);
		// }
		// TODO
		return result;
	}

	@Override
	public URI inJvm(String url) {
		return this.hook.inJvm(url);
	}

	@Override
	public Object inJvmCall(URI uri, Object request) {
		return this.hook.inJvmCall(uri, request);
	}

	@Override
	public void setHook(Object obj) {
		this.hook = (JPCBridge) obj;
		RpcProxy.setJpc(this);
	}

	@Override
	public Object getServiceInfo() {
		return TangYuanContainer.getInstance().getServicesKeySet();
	}

}
