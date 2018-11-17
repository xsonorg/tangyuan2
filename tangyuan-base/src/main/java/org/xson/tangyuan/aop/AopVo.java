package org.xson.tangyuan.aop;

import java.util.List;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.util.PatternMatchUtils;
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

		BEFORE(1), AFTER_EXTEND(2), AFTER(4);

		private int value = 0;

		private PointCut(int value) {
			this.value = value;
		}

		public int value() {
			return this.value;
		}
	}

	protected String		exec;
	protected int			order;
	protected CallMode		mode;
	protected List<String>	includeList;
	protected List<String>	excludeList;

	public boolean match(String url) {
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

	/**
	 * 之前前置方法
	 * 
	 * @param service 仅作日志标识使用
	 * @param pkgArg 封装后的对象
	 */
	protected void execBefore(Object pkgArg) {
		throw new TangYuanException("Subclasses must override this method");
	}

	protected void execAfter(ServiceContext parent, Object pkgArg, Throwable ex) {
		throw new TangYuanException("Subclasses must override this method");
	}

	abstract protected Log getLog();

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

}
