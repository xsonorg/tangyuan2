package org.xson.tangyuan.es.xml.node;

import org.xson.common.object.XCO;
import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.es.EsResultConverter;
import org.xson.tangyuan.es.datasource.EsSourceVo;
import org.xson.tangyuan.es.util.EsHttpResultWrapper;
import org.xson.tangyuan.executor.ServiceException;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class EsDeleteNode extends AbstractEsNode {

	private static Log log = LogFactory.getLog(EsDeleteNode.class);

	public EsDeleteNode(String id, String ns, String serviceKey, String dsKey, TangYuanNode sqlNode, EsResultConverter converter, CacheCleanVo cacheClean, String desc,
			String[] groups) {
		this.id = id;
		this.ns = ns;
		this.serviceKey = serviceKey;
		this.dsKey = dsKey;
		this.resultType = XCO.class;
		this.sqlNode = sqlNode;
		this.converter = converter;

		this.cacheClean = cacheClean;
		this.desc = desc;
		this.groups = groups;
	}

	@Override
	protected Log getLog() {
		return log;
	}

	@Override
	protected Object executeCommand(ActuatorContext ac, EsSourceVo esSourceVo, String url, String body) throws Throwable {
		EsHttpResultWrapper resultWrapper = esSourceVo.getClient().delete(url);
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

	//	public EsDeleteNode(String id, String ns, String serviceKey, String dsKey, TangYuanNode sqlNode, ResultConverter converter) {
	//		this.id = id;
	//		this.ns = ns;
	//		this.serviceKey = serviceKey;
	//		this.dsKey = dsKey;
	//		this.resultType = XCO.class;
	//		this.sqlNode = sqlNode;
	//		this.converter = converter;
	//	}
	//	@Override
	//	public boolean execute(ServiceContext context, Object arg) throws Throwable {
	//		EsServiceContext esContext = (EsServiceContext) context.getServiceContext(TangYuanServiceType.ES);
	//
	//		long             startTime = System.currentTimeMillis();
	//		Object           result    = null;
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
	//			log.info("DELETE " + url);
	//		}
	//
	//		String json = esSourceVo.getClient().delete(url);
	//		result = converter.convert(json);
	//
	//		context.setResult(result);
	//
	//		if (log.isInfoEnabled()) {
	//			log.info("es execution time: " + getSlowServiceLog(startTime));
	//		}
	//
	//		return true;
	//	}

}
