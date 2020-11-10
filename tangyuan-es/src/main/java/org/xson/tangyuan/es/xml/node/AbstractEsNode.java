package org.xson.tangyuan.es.xml.node;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.es.EsComponent;
import org.xson.tangyuan.es.EsResultConverter;
import org.xson.tangyuan.es.datasource.EsSourceManager;
import org.xson.tangyuan.es.datasource.EsSourceVo;
import org.xson.tangyuan.es.util.ESUtil;
import org.xson.tangyuan.es.util.EsHttpResultWrapper;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.ext.LogExtUtil;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.service.context.EsServiceContext;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.xml.node.AbstractServiceNode;
import org.xson.tangyuan.xml.node.TangYuanNode;

public abstract class AbstractEsNode extends AbstractServiceNode {

	protected TangYuanNode      sqlNode         = null;

	protected String            dsKey           = null;
	protected boolean           simple          = true;

	protected EsResultConverter converter       = null;

	protected CacheUseVo        cacheUse        = null;
	protected CacheCleanVo      cacheClean      = null;

	protected EsSourceManager   esSourceManager = null;

	protected AbstractEsNode() {
		this.serviceType = TangYuanServiceType.ES;
		esSourceManager = EsComponent.getInstance().getEsSourceManager();
	}

	public String getDsKey() {
		return dsKey;
	}

	public boolean isSimple() {
		return simple;
	}

	protected void removeCache(ActuatorContext ac, String cacheKey) {
		ac.addPostTask(new Runnable() {
			@Override
			public void run() {
				cacheClean.removeObject(cacheKey);
			}
		});
	}

	protected void putCache(ActuatorContext ac, String cacheKey, Object result) {
		ac.addPostTask(new Runnable() {
			@Override
			public void run() {
				cacheUse.putObject(cacheKey, result);
			}
		});
	}

	protected Log getLog() {
		return null;
	}

	public boolean execute(ActuatorContext ac, Object arg, Object temp) throws Throwable {

		EsServiceContext context   = (EsServiceContext) ac.getServiceContext(this.serviceType);
		long             startTime = System.currentTimeMillis();
		String           cacheKey  = null;
		Object           result    = null;

		// 1. cache使用
		if (null != this.cacheUse && null == cacheKey) {
			cacheKey = this.cacheUse.buildKey(arg);
		}
		if (null != this.cacheClean && null == cacheKey) {
			cacheKey = this.cacheClean.buildKey(arg);
		}
		if (null != this.cacheUse) {
			result = this.cacheUse.getObject(cacheKey);
			if (null != result) {
				ac.setResult(result);
				if (getLog().isInfoEnabled()) {
					getLog().info("es execution time: " + getSlowServiceLog(startTime) + " use cache");
				}
				return true;
			}
		}

		// 0. 克隆参数
		//		if (null == temp) {
		//			temp = cloneArg(arg);
		//		}

		//		EsSourceVo esSourceVo = EsSourceManager.getEsSource(this.dsKey);
		//		EsSourceVo esSourceVo = EsComponent.getInstance().getEsSourceManager().getEsSource(this.dsKey);
		EsSourceVo esSourceVo = this.esSourceManager.getEsSource(this.dsKey);

		// 1. 解析URL
		String     url        = parseURL(ac, context, esSourceVo, arg, temp);

		// 2. 解析Body
		String     body       = parseBody(ac, context, esSourceVo, arg, temp);

		// 3. 调用ES
		result = executeCommand(ac, esSourceVo, url, body);

		if (getLog().isInfoEnabled()) {
			getLog().info("es execution time: " + getSlowServiceLog(startTime));
		}

		// 8. 放置缓存
		if (null != cacheUse) {
			putCache(ac, cacheKey, result);
		}
		// 8. 清理缓存
		if (null != cacheClean) {
			removeCache(ac, cacheKey);
		}

		return true;
	}

	protected String parseURL(ActuatorContext ac, EsServiceContext context, EsSourceVo esSourceVo, Object arg, Object temp) throws Throwable {
		//		throw new TangYuanException("subclass must implement this method");
		context.resetExecEnv();
		context.setIgnoreQuotes();
		sqlNode.execute(ac, arg, temp);
		String url = StringUtils.trim(context.getSql());
		url = ESUtil.mergeURL(esSourceVo.getHost(), url);
		//		if (getLog().isInfoEnabled()) {
		//			// trace
		//			getLog().info("GET " + url);
		//		}
		return url;
	}

	protected String parseBody(ActuatorContext ac, EsServiceContext context, EsSourceVo esSourceVo, Object arg, Object temp) throws Throwable {
		return null;
	}

	protected Object executeCommand(ActuatorContext ac, EsSourceVo esSourceVo, String url, String body) throws Throwable {
		throw new TangYuanException("subclass must implement this method");
	}

	protected void printCommand(String url, String body, String result) {
		if (!getLog().isInfoEnabled()) {
			return;
		}
		getLog().info("es request url:" + url);
		if (null != body) {
			getLog().info("es request body:\n " + body);
		}
		if (LogExtUtil.isEsResponseResultPrint()) {
			getLog().info("es response:\n " + result);
		}
	}

	protected void printCommand(String url, String body, EsHttpResultWrapper resultWrapper) {
		if (!getLog().isInfoEnabled()) {
			return;
		}
		getLog().info("es request url: " + url);
		if (null != body) {
			getLog().info("es request body:\n" + body);
		}
		if (LogExtUtil.isEsResponseResultPrint()) {
			if (200 == resultWrapper.getHttpState() || 201 == resultWrapper.getHttpState()) {
				getLog().info("es response state: " + resultWrapper.getHttpState());
				getLog().info("es response content:\n" + resultWrapper.getContent());
			} else {
				getLog().warn("es response state: " + resultWrapper.getHttpState());
				getLog().warn("es response content:\n" + resultWrapper.getContent());
			}
		}
	}

	protected Object convert(EsHttpResultWrapper resultWrapper) throws Throwable {
		if (200 == resultWrapper.getHttpState()) {
			return this.converter.convert(resultWrapper.getContent());
		} else {
			return this.converter.convertOnError(resultWrapper.getContent(), resultWrapper.getHttpState());
		}
	}
}
