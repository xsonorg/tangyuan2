package org.xson.tangyuan.aop;

import org.xson.tangyuan.xml.node.AbstractServiceNode;

public interface Aop {

	void execBefore(AbstractServiceNode service, Object arg) throws Throwable;

	void execAfter(AbstractServiceNode service, Object arg, Object result, Throwable ex) throws Throwable;

	void bindDynamic(AbstractServiceNode serviceNode);

}
