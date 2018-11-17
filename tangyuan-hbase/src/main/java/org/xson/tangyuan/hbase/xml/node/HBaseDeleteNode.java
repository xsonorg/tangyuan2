package org.xson.tangyuan.hbase.xml.node;

import org.xson.common.object.XCO;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.hbase.executor.HBaseServiceContext;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class HBaseDeleteNode extends AbstractHBaseNode {

	private static Log log = LogFactory.getLog(HBaseDeleteNode.class);

	public HBaseDeleteNode(String id, String ns, String serviceKey, String dsKey, TangYuanNode sqlNode) {
		this.id = id;
		this.ns = ns;
		this.serviceKey = serviceKey;
		this.dsKey = dsKey;
		this.resultType = XCO.class;

		this.sqlNode = sqlNode;
	}

	@Override
	public boolean execute(ServiceContext context, Object arg) throws Throwable {
		HBaseServiceContext xContext = (HBaseServiceContext) context.getServiceContext(TangYuanServiceType.HBASE);

		long startTime = System.currentTimeMillis();
		Object result = null;

		// 2. 清理和重置执行环境
		xContext.resetExecEnv();
		sqlNode.execute(context, arg); // 获取URL
		String sql = xContext.getSql();

		if (log.isInfoEnabled()) {
			log.info(sql);
		}

		result = xContext.getActuator().delete(this.dsKey, StringUtils.trim(sql));
		context.setResult(result);

		if (log.isInfoEnabled()) {
			log.info("hbase execution time: " + getSlowServiceLog(startTime));
		}

		return true;
	}

}
