package org.xson.tangyuan.es.xml.node;

import org.xson.common.object.XCO;
import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.es.EsResultConverter;
import org.xson.tangyuan.es.datasource.EsSourceVo;
import org.xson.tangyuan.es.util.EsHttpResultWrapper;
import org.xson.tangyuan.executor.ServiceException;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class EsGetNode extends AbstractEsNode {

	private static Log log = LogFactory.getLog(EsGetNode.class);

	public EsGetNode(String id, String ns, String serviceKey, String dsKey, TangYuanNode sqlNode, EsResultConverter converter, CacheUseVo cacheUse, String desc, String[] groups) {
		this.id = id;
		this.ns = ns;
		this.serviceKey = serviceKey;
		this.dsKey = dsKey;
		this.resultType = XCO.class;

		this.sqlNode = sqlNode;
		this.cacheUse = cacheUse;
		this.converter = converter;

		this.desc = desc;
		this.groups = groups;
	}

	@Override
	protected Log getLog() {
		return log;
	}

	@Override
	protected Object executeCommand(ActuatorContext ac, EsSourceVo esSourceVo, String url, String body) throws Throwable {
		//		String json = esSourceVo.getClient().get(url);
		EsHttpResultWrapper resultWrapper = esSourceVo.getClient().get(url);
		printCommand(url, body, resultWrapper);
		try {
			Object result = convert(resultWrapper);
			ac.setResult(result);
			return result;
		} catch (Throwable e) {
			if (e instanceof ServiceException) {
				throw e;
			}
			throw new ServiceException(TangYuanLang.get("convert.result.x.error", "es"), e);
		}
	}

	//	@Override
	//	protected Object executeCommand(ActuatorContext ac, EsSourceVo esSourceVo, String url, String body) throws Throwable {
	//		String json = esSourceVo.getClient().get(url);
	//		printCommand(url, body, json);
	//		try {
	//			Object result = converter.convert(json);
	//			ac.setResult(result);
	//			return result;
	//		} catch (Throwable e) {
	//			throw new ServiceException(TangYuanLang.get("convert.result.x.error", "es"), e);
	//		}
	//	}

	//	protected String parseURL(ActuatorContext ac, EsServiceContext context, EsSourceVo esSourceVo, Object arg, Object temp) throws Throwable {
	//		context.resetExecEnv();
	//		context.setIgnoreQuotes();
	//		sqlNode.execute(ac, arg, temp);
	//		String url = StringUtils.trim(context.getSql());
	//		url = ESUtil.mergeURL(esSourceVo.getHost(), url);
	//		if (getLog().isInfoEnabled()) {
	//			//  trace
	//			getLog().info("GET " + url);
	//		}
	//		return url;
	//	}

	//	protected String parseBody(ActuatorContext ac, EsServiceContext context, EsSourceVo esSourceVo, Object arg, Object temp) {
	//		//		sqlNode.execute(ac, arg, temp);
	//		//		if (getLog().isInfoEnabled() || context.isTraceCommand()) {
	//		//			// 准备日志输出的SQL
	//		//			context.parseSqlLog();
	//		//		}
	//		return null;
	//	}

	//	public boolean execute(ActuatorContext ac, Object arg, Object temp) throws Throwable {
	//
	//		EsServiceContext context   = (EsServiceContext) ac.getServiceContext(this.serviceType);
	//		long             startTime = System.currentTimeMillis();
	//		String           cacheKey  = null;
	//		Object           result    = null;
	//
	//		// 1. cache使用
	//		if (null != this.cacheUse && null == cacheKey) {
	//			cacheKey = this.cacheUse.buildKey(arg);
	//		}
	//		if (null != this.cacheClean && null == cacheKey) {
	//			cacheKey = this.cacheClean.buildKey(arg);
	//		}
	//		if (null != this.cacheUse) {
	//			result = this.cacheUse.getObject(cacheKey);
	//			if (null != result) {
	//				ac.setResult(result);
	//				if (getLog().isInfoEnabled()) {
	//					getLog().info("es execution time: " + getSlowServiceLog(startTime));
	//				}
	//				return true;
	//			}
	//		}
	//
	//		// 0. 克隆参数
	//		if (null == temp) {
	//			temp = cloneArg(arg);
	//		}
	//
	//		EsSourceVo esSourceVo = EsSourceManager.getEsSource(this.dsKey);
	//
	//		// 1. 解析URL
	//		String     url        = parseURL(ac, context, esSourceVo, arg, temp);
	//
	//		// 2. 解析Body
	//		String     body       = parseBody(ac, context, esSourceVo, arg, temp);
	//
	//		// 3. 调用ES
	//		result = executeSql(ac, esSourceVo, url, body);
	//
	//		if (getLog().isInfoEnabled()) {
	//			getLog().info("es execution time: " + getSlowServiceLog(startTime));
	//		}
	//
	//		// 8. 放置缓存
	//		if (null != cacheUse) {
	//			putCache(ac, cacheKey, result);
	//		}
	//		// 8. 清理缓存
	//		if (null != cacheClean) {
	//			removeCache(ac, cacheKey);
	//		}
	//
	//		return true;
	//	}

	//	@Override
	//	public boolean execute(ServiceContext context, Object arg) throws Throwable {
	////		EsServiceContext esContext = (EsServiceContext) context.getServiceContext(TangYuanServiceType.ES);
	////		// 1. cache使用
	////		if (null != cacheUse) {
	////			Object result = cacheUse.getObject(arg);
	////			if (null != result) {
	////				context.setResult(result);
	////				return true;
	////			}
	////		}
	//
	////		long   startTime = System.currentTimeMillis();
	////		Object result    = null;
	//
	//		// 2. 清理和重置执行环境
	//		esContext.resetExecEnv();
	//		esContext.setIgnoreQuotes();
	//		sqlNode.execute(context, arg); // 获取URL
	//		String     url        = StringUtils.trim(esContext.getSql());
	//
	//		EsSourceVo esSourceVo = EsSourceManager.getEsSource(this.dsKey);
	//		url = ESUtil.mergeURL(esSourceVo.getHost(), url);
	//
	//		if (log.isInfoEnabled()) {
	//			log.info("GET " + url);
	//		}
	//
	//		String json = esSourceVo.getClient().get(url);
	//		result = converter.convert(json);
	//
	//		context.setResult(result);
	//
	//		if (log.isInfoEnabled()) {
	//			log.info("es execution time: " + getSlowServiceLog(startTime));
	//		}
	//
	//		if (null != cacheUse) {
	//			cacheUse.putObject(arg, result);
	//		}
	//
	//		return true;
	//	}
	//	private CacheUseVo	cacheUse;
	//	public EsGetNode(String id, String ns, String serviceKey, String dsKey, TangYuanNode sqlNode, CacheUseVo cacheUse, ResultConverter converter) {
	//		this.id = id;
	//		this.ns = ns;
	//		this.serviceKey = serviceKey;
	//		this.dsKey = dsKey;
	//		this.resultType = XCO.class;
	//
	//		this.sqlNode = sqlNode;
	//		this.cacheUse = cacheUse;
	//		this.converter = converter;
	//	}
}
