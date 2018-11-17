package org.xson.tangyuan.es.xml.node;

import org.xson.common.object.XCO;
import org.xson.tangyuan.es.ResultConverter;
import org.xson.tangyuan.es.datasource.EsSourceManager;
import org.xson.tangyuan.es.datasource.EsSourceVo;
import org.xson.tangyuan.es.executor.EsServiceContext;
import org.xson.tangyuan.es.util.ESUtil;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class EsDeleteNode extends AbstractEsNode {

	private static Log log = LogFactory.getLog(EsDeleteNode.class);

	public EsDeleteNode(String id, String ns, String serviceKey, String dsKey, TangYuanNode sqlNode, ResultConverter converter) {
		this.id = id;
		this.ns = ns;
		this.serviceKey = serviceKey;
		this.dsKey = dsKey;
		this.resultType = XCO.class;
		this.sqlNode = sqlNode;
		this.converter = converter;
	}

	@Override
	public boolean execute(ServiceContext context, Object arg) throws Throwable {
		EsServiceContext esContext = (EsServiceContext) context.getServiceContext(TangYuanServiceType.ES);

		long startTime = System.currentTimeMillis();
		Object result = null;

		// 2. 清理和重置执行环境
		esContext.resetExecEnv();
		esContext.setIgnoreQuotes();
		sqlNode.execute(context, arg); // 获取URL
		String url = StringUtils.trim(esContext.getSql());

		EsSourceVo esSourceVo = EsSourceManager.getEsSource(this.dsKey);
		url = ESUtil.mergeURL(esSourceVo.getHost(), url);

		if (log.isInfoEnabled()) {
			log.info("DELETE " + url);
		}

		String json = esSourceVo.getClient().delete(url);
		result = converter.convert(json);

		context.setResult(result);

		if (log.isInfoEnabled()) {
			log.info("es execution time: " + getSlowServiceLog(startTime));
		}

		return true;
	}

}
