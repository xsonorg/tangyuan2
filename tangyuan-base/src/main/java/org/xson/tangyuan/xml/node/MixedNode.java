package org.xson.tangyuan.xml.node;

import java.util.List;

import org.xson.tangyuan.executor.ServiceContext;

public class MixedNode implements TangYuanNode {

	private List<TangYuanNode> contents;

	public MixedNode(List<TangYuanNode> contents) {
		this.contents = contents;
	}

	@Override
	public boolean execute(ServiceContext context, Object arg) throws Throwable {
		for (TangYuanNode sqlNode : contents) {
			sqlNode.execute(context, arg);
		}
		return true;
	}
}