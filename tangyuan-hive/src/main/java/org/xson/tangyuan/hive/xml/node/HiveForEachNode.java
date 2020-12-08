package org.xson.tangyuan.hive.xml.node;

import org.xson.tangyuan.hive.service.context.HiveServiceContext;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;
import org.xson.tangyuan.xml.node.ForEachNode;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class HiveForEachNode extends ForEachNode {

	public HiveForEachNode(TangYuanNode sqlNode, Variable collection, String index, String open, String close, String separator) {
		this.sqlNode = sqlNode;
		this.collection = collection;
		this.index = index;
		this.open = open;
		this.close = close;
		this.separator = separator;
	}

	protected void append(ActuatorContext ac, String str) {
		HiveServiceContext serviceContext = (HiveServiceContext) ac.getServiceContext(TangYuanServiceType.HIVE);
		if (null != str && str.length() > 0) {
			serviceContext.addSql(str);
		}
	}

	//	public HiveForEachNode(TangYuanNode sqlNode, Variable collection, String index, String open, String close, String separator, Object start,
	//			Object end, Object pLen, boolean ignoreIOOB, int indexMode) {
	//		this.sqlNode = sqlNode;
	//		this.collection = collection;
	//		this.index = index;
	//		this.open = open;
	//		this.close = close;
	//		this.separator = separator;
	//
	//		this.start = start;
	//		this.end = end;
	//		this.pLen = pLen;
	//		this.ignoreIOOB = ignoreIOOB;
	//		this.indexMode = indexMode;
	//	}
	//
	//	protected void append(ServiceContext serviceContext, String str) {
	//
	//		HiveServiceContext sqlContext = (HiveServiceContext) serviceContext.getServiceContext(TangYuanServiceType.HIVE);
	//
	//		if (null != str && str.length() > 0) {
	//			sqlContext.addSql(str);
	//		}
	//	}
}
