package org.xson.tangyuan.hbase.xml.node;

import org.xson.common.object.XCO;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.hbase.executor.HBaseServiceContext;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class HBasePutBatchNode extends AbstractHBaseNode {

	private static Log	log	= LogFactory.getLog(HBasePutBatchNode.class);

	private boolean		async;

	public HBasePutBatchNode(String id, String ns, String serviceKey, String dsKey, boolean async, TangYuanNode sqlNode) {
		this.id = id;
		this.ns = ns;
		this.serviceKey = serviceKey;
		this.dsKey = dsKey;
		this.resultType = XCO.class;
		this.async = async;
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

		if (this.async) {
			result = xContext.getActuator().putBatchAsync(this.dsKey, xContext.getSqlList());
		} else {
			result = xContext.getActuator().putBatchSynch(this.dsKey, xContext.getSqlList());
		}

		context.setResult(result);

		if (log.isInfoEnabled()) {
			log.info("hbase execution time: " + getSlowServiceLog(startTime));
		}

		return true;
	}

}
