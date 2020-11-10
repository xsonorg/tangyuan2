package org.xson.tangyuan.aop.service.vo;

import java.util.List;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.aop.service.ServiceAfterAopHandler;
import org.xson.tangyuan.aop.service.ServiceBeforeAopHandler;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.util.PatternMatchUtils;
import org.xson.tangyuan.xml.node.AbstractServiceNode;
import org.xson.tangyuan.xml.node.CallNode.CallMode;

public abstract class AopVo implements Comparable<AopVo> {

	public enum AopCondition {
		/** 成功 */
		SUCCESS,

		/** 异常 */
		EXCEPTION,

		/** 所有 */
		ALL
	}

	public enum PointCut {

		//		BEFORE(1), AFTER_EXTEND(2), AFTER(4);
		BEFORE(1), AFTER(2);

		private int value = 0;

		private PointCut(int value) {
			this.value = value;
		}

		public int value() {
			return this.value;
		}
	}

	protected String                  exec;
	protected ServiceBeforeAopHandler beforeHandler;
	protected ServiceAfterAopHandler  afterHandler;

	protected int                     order;
	protected CallMode                mode;
	protected List<String>            includeList;
	protected List<String>            excludeList;

	public boolean match(AbstractServiceNode serviceNode) {

		// TODO
		String url = serviceNode.getServiceKey();
		// 排除递归
		if (exec.equalsIgnoreCase(url)) {
			return false;
		}
		if (null != excludeList) {
			for (String pattern : excludeList) {
				if (PatternMatchUtils.simpleMatch(pattern, url)) {
					return false;
				}
			}
		}
		if (null != includeList) {
			for (String pattern : includeList) {
				if (PatternMatchUtils.simpleMatch(pattern, url)) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

	protected void execBefore(String serviceURI, XCO arg) {
		throw new TangYuanException(TangYuanLang.get("method.need.override"));
	}

	protected void execAfter(String serviceURI, XCO arg, XCO result, Throwable ex) {
		throw new TangYuanException(TangYuanLang.get("method.need.override"));
	}

	public int getOrder() {
		return order;
	}

	public String getExec() {
		return exec;
	}

	@Override
	public int compareTo(AopVo o) {
		// 比较此对象与指定对象的顺序。如果该对象小于、等于或大于指定对象，则分别返回负整数、零或正整数。
		if (this.order < o.getOrder()) {
			return -1;
		} else if (this.order > o.getOrder()) {
			return 1;
		} else {
			return 0;
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////

	//	protected ActuatorImpl            actuator;
	//	public boolean match1(String url) {
	//		// 排除递归
	//		if (exec.equalsIgnoreCase(url)) {
	//			return false;
	//		}
	//		if (null != excludeList) {
	//			for (String pattern : excludeList) {
	//				if (PatternMatchUtils.simpleMatch(pattern, url)) {
	//					return false;
	//				}
	//			}
	//		}
	//		if (null != includeList) {
	//			for (String pattern : includeList) {
	//				if (PatternMatchUtils.simpleMatch(pattern, url)) {
	//					return true;
	//				}
	//			}
	//			return false;
	//		}
	//		return true;
	//	}

	//	/**
	//	 * 之前前置方法
	//	 * 
	//	 * @param service 仅作日志标识使用
	//	 * @param pkgArg 封装后的对象
	//	 */
	//	protected void execBefore(Object pkgArg) {
	//		throw new TangYuanException(TangYuanLang.get("method.need.override"));
	//	}
	//	protected void execAfter(ActuatorContext parent, Object pkgArg, Throwable ex) {
	//		throw new TangYuanException(TangYuanLang.get("method.need.override"));
	//	}

}
