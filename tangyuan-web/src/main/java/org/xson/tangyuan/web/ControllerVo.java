package org.xson.tangyuan.web;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.xson.common.object.XCO;
import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.service.Actuator;
import org.xson.tangyuan.web.RequestContext.RequestTypeEnum;
import org.xson.tangyuan.web.util.WebUtil;
import org.xson.tangyuan.web.xml.vo.MethodObject;

public class ControllerVo {

	private static Log           log             = LogFactory.getLog(ControllerVo.class);

	protected String             url             = null;
	protected String             transfer        = null;
	protected String             validate        = null;
	protected MethodObject       execMethod      = null;
	protected List<MethodObject> assemblyMethods = null;
	protected List<MethodObject> beforeMethods   = null;
	protected List<MethodObject> afterMethods    = null;
	/** 权限设置: 用户可自行处理 */
	protected String             permission      = null;
	protected CacheUseVo         cacheUse        = null;
	protected boolean            cacheInAop      = false;
	/** 数据转换器 */
	protected DataConverter      dataConverter   = null;
	protected RequestTypeEnum    requestType     = null;
	/** 返回结果处理器 */
	protected ResponseHandler    responseHandler = null;
	/** 服务的描述 */
	protected String             desc            = null;
	/** 服务组名 */
	protected String[]           groups          = null;

	public ControllerVo(String url, RequestTypeEnum requestType, String transfer, String validate, MethodObject execMethod, List<MethodObject> assemblyMethods,
			List<MethodObject> beforeMethods, List<MethodObject> afterMethods, String permission, CacheUseVo cacheUse, DataConverter dataConverter, boolean cacheInAop,
			ResponseHandler responseHandler, String desc, String[] groups) {
		this.url = url;
		this.requestType = requestType;
		this.transfer = transfer;
		this.validate = validate;
		this.execMethod = execMethod;
		this.assemblyMethods = assemblyMethods;
		this.beforeMethods = beforeMethods;
		this.afterMethods = afterMethods;

		this.permission = permission;
		this.cacheUse = cacheUse;
		this.dataConverter = dataConverter;
		this.cacheInAop = cacheInAop;

		this.responseHandler = responseHandler;

		this.desc = desc;
		this.groups = groups;
	}

	public String getUrl() {
		return url;
	}

	public String getTransfer() {
		return transfer;
	}

	public String getValidate() {
		return validate;
	}

	public String getPermission() {
		return permission;
	}

	public CacheUseVo getCacheUse() {
		return cacheUse;
	}

	public boolean isCacheInAop() {
		return cacheInAop;
	}

	public RequestTypeEnum getRequestType() {
		return requestType;
	}

	public ResponseHandler getResponseHandler() {
		return responseHandler;
	}

	public String[] getGroups() {
		return groups;
	}

	public String getDesc() {
		return desc;
	}

	protected void dataConvert(RequestContext context) throws Throwable {
		if (null != this.dataConverter) {
			this.dataConverter.convert(context);
			return;
		}
		DataConverter tempConverter = WebUtil.getDefaultDataConverter(this, context.getContextType());
		if (null != tempConverter) {
			tempConverter.convert(context);
		}
	}

	protected void assembly(RequestContext context) throws Throwable {
		if (null != this.assemblyMethods) {
			try {
				for (MethodObject mo : this.assemblyMethods) {
					mo.getMethod().invoke(mo.getInstance(), context);
				}
			} catch (Throwable e) {
				// fix bug
				if (e instanceof InvocationTargetException) {
					throw ((InvocationTargetException) e).getTargetException();
				}
				throw e;
			}
		}
	}

	protected void before(RequestContext context) throws Throwable {
		try {
			if (null != this.beforeMethods) {
				for (MethodObject mo : this.beforeMethods) {
					mo.getMethod().invoke(mo.getInstance(), context);
				}
			}
		} catch (Throwable e) {
			if (e instanceof InvocationTargetException) {
				throw ((InvocationTargetException) e).getTargetException();
			}
			throw e;
		}
	}

	protected void after(RequestContext context) throws Throwable {
		try {
			if (null != this.afterMethods) {
				for (MethodObject mo : this.afterMethods) {
					mo.getMethod().invoke(mo.getInstance(), context);
				}
			}
		} catch (Throwable e) {
			if (e instanceof InvocationTargetException) {
				throw ((InvocationTargetException) e).getTargetException();
			}
			throw e;
		}
	}

	protected void exec(RequestContext context) throws Throwable {
		try {
			if (null != this.execMethod) {
				Object retObj = this.execMethod.getMethod().invoke(this.execMethod.getInstance(), context);

				if (null != context.getResult()) {
					return;
				}

				context.setResult(retObj);

				// if (null == context.getResult() && null != retObj) {
				// context.setResult(TangYuanUtil.retObjToXco(retObj));
				// }
				// fix bug 这里已经做转换了，后面需要注意
				//				if (null == context.getResult()) {
				//					context.setResult(TangYuanUtil.retObjToXco(retObj));
				//				}

			} else {
				XCO request = (XCO) context.getArg();
				if (null == request) {
					request = new XCO();
				}

				//				XCO    result = null;
				//				// 统一服务调用
				//				Object retObj = Actuator.execute(transfer, request);
				//				result = TangYuanUtil.retObjToXco(retObj);

				XCO result = Actuator.execute(transfer, request);
				context.setResult(result);
			}
		} catch (Throwable e) {
			if (e instanceof InvocationTargetException) {
				throw ((InvocationTargetException) e).getTargetException();
			}
			throw e;
		}
	}

	private void buildCacheKey(RequestContext context) {
		String cacheKey = cacheUse.buildKey(context.getArg());
		context.setCacheKey(cacheKey);
	}

	protected boolean cacheGet(RequestContext context) {
		if (null != cacheUse) {
			// 构建key
			buildCacheKey(context);
			Object result = null;
			try {
				result = cacheUse.getObject(context.getCacheKey());
			} catch (Throwable e) {
				log.error("cache get error.", e);
			}
			if (null != result) {
				context.setResult(result);
				return true;
			}
		}
		return false;
	}

	protected void cachePut(RequestContext context) {
		if (null != cacheUse) {
			try {
				cacheUse.putObject(context.getCacheKey(), context.getResult());
			} catch (Throwable e) {
				log.error("cache put error.", e);
			}
		}
	}

	//////////////////////////////////////////////////////////

	// public boolean cacheGet(RequestContext context) throws Throwable {
	// if (null != cacheUse) {
	// Object result = cacheUse.getObject(context.getArg());
	// if (null != result) {
	// context.setResult(result);
	// return true;
	// }
	// }
	// return false;
	// }
	//
	// public void cachePut(RequestContext context) throws Throwable {
	// if (null != cacheUse) {
	// cacheUse.putObject(context.getArg(), context.getResult());
	// }
	// }
	// public boolean existDataConverter() {
	// return null == this.dataConverter ? false : true;
	// }

	//	public void exec(RequestContext context) throws Throwable {
	//		try {
	//			if (null != this.execMethod) {
	//				Object retObj = this.execMethod.getMethod().invoke(this.execMethod.getInstance(), context);
	//				// if (null == context.getResult() && null != retObj) {
	//				// context.setResult(TangYuanUtil.retObjToXco(retObj));
	//				// }
	//				// fix bug 这里已经做转换了，后面需要注意 
	//				if (null == context.getResult()) {
	//					context.setResult(TangYuanUtil.retObjToXco(retObj));
	//				}
	//			} else {
	//				XCO request = (XCO) context.getArg();
	//				if (null == request) {
	//					request = new XCO();
	//				}
	//				XCO    result = null;
	//				// 统一服务调用
	//				Object retObj = Actuator.execute(transfer, request);
	//				result = TangYuanUtil.retObjToXco(retObj);
	//				context.setResult(result);
	//			}
	//		} catch (Throwable e) {
	//			if (e instanceof InvocationTargetException) {
	//				throw ((InvocationTargetException) e).getTargetException();
	//			}
	//			throw e;
	//		}
	//	}
}
