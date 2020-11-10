package org.xson.tangyuan.xml.node;

import java.util.List;

import org.xson.tangyuan.service.ActuatorContext;

public class MixedNode implements TangYuanNode {

	private List<TangYuanNode> contents;

	public MixedNode(List<TangYuanNode> contents) {
		this.contents = contents;
	}

	@Override
	public boolean execute(ActuatorContext ac, Object arg, Object temp) throws Throwable {
		for (TangYuanNode sqlNode : contents) {
			sqlNode.execute(ac, arg, temp);
		}
		return true;
	}

}