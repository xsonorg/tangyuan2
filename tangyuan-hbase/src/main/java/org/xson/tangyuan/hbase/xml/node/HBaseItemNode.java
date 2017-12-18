package org.xson.tangyuan.hbase.xml.node;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.hbase.executor.HBaseServiceContext;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class HBaseItemNode implements TangYuanNode {

	private static Log		log		= LogFactory.getLog(HBaseItemNode.class);

	protected TangYuanNode	sqlNode	= null;

	public HBaseItemNode(TangYuanNode sqlNode) {
		this.sqlNode = sqlNode;
	}

	@Override
	public boolean execute(ServiceContext context, Object arg) throws Throwable {
		sqlNode.execute(context, arg);

		HBaseServiceContext xServiceContext = (HBaseServiceContext) context.getServiceContext(TangYuanServiceType.HBASE);
		String sql = xServiceContext.getSql();
		if (log.isInfoEnabled()) {
			log.info(sql);
		}
		xServiceContext.appendAndClean(sql);

		return true;
	}
}
