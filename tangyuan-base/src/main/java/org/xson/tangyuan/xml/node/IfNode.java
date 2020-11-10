package org.xson.tangyuan.xml.node;

import java.util.ArrayList;
import java.util.List;

import org.xson.tangyuan.ognl.vars.vo.LogicalVariable;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.xml.XmlParseException;

public class IfNode implements TangYuanNode {

	private LogicalVariable	test;

	private TangYuanNode	sqlNode;

	private List<IfNode>	elseIfList;

	private boolean			hasElseNode	= false;

	public IfNode(TangYuanNode sqlNode, LogicalVariable test) {
		this.sqlNode = sqlNode;
		this.test = test;
	}

	public boolean isHasElseNode() {
		return hasElseNode;
	}

	public void addElseNode(IfNode node) {
		if (hasElseNode) {
			throw new XmlParseException("当前节点不能再加入else node");
		}
		addElseIfNode(node);
		hasElseNode = true;
	}

	public void addElseIfNode(IfNode node) {
		if (hasElseNode) {
			throw new XmlParseException("当前节点不能再加入else if node");
		}
		if (null == elseIfList) {
			elseIfList = new ArrayList<IfNode>();
		}
		elseIfList.add(node);
	}

	/**
	 * true: 代表执行了(表达式通过), false: 代表不能执行(表达式不通过)
	 */
	@Override
	public boolean execute(ActuatorContext ac, Object arg, Object temp) throws Throwable {
		// 这里可以认为全部是IF, 表达式通过:true, 否则:false
		if (null == test || test.getResult(arg)) {
			sqlNode.execute(ac, arg, temp);
			return true;
		} else if (null != elseIfList) {
			for (IfNode ifNode : elseIfList) {
				if (ifNode.execute(ac, arg, temp)) {
					return true;
				}
			}
		}
		return false;
	}

}
