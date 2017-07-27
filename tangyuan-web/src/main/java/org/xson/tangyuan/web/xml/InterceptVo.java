package org.xson.tangyuan.web.xml;

import java.util.List;

import org.xson.tangyuan.util.PatternMatchUtils;

/**
 * 前置、后置、数据组装处理器VO
 */
public class InterceptVo implements Comparable<InterceptVo> {

	/**
	 * 拦截节点类型
	 */
	public enum InterceptType {
		ASSEMBLY, BEFORE, AFTER
	}

	private MethodObject	mo;
	private int				order;
	private List<String>	includeList;
	private List<String>	excludeList;

	public InterceptVo(MethodObject mo, int order, List<String> includeList, List<String> excludeList) {
		this.mo = mo;
		this.order = order;
		this.includeList = includeList;
		this.excludeList = excludeList;
	}

	public InterceptVo(MethodObject mo, int order) {
		this.mo = mo;
		this.order = order;
	}

	// public boolean match(String url) {
	// if (null != excludeList) {
	// for (String pattern : excludeList) {
	// if (PatternMatchUtils.simpleMatch(pattern, url)) {
	// return false;
	// }
	// }
	// }
	// if (null != includeList) {
	// for (String pattern : includeList) {
	// if (!PatternMatchUtils.simpleMatch(pattern, url)) {
	// return false;
	// }
	// }
	// }
	// return true;
	// }

	public boolean match(String url) {
		if (null != excludeList) {
			for (String pattern : excludeList) {
				if (PatternMatchUtils.simpleMatch(pattern, url)) {
					return false;
				}
			}
		}
		// fix bug
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

	public MethodObject getMo() {
		return mo;
	}

	@Override
	public int compareTo(InterceptVo o) {
		if (this.order > o.order) {
			return 1;
		} else if (this.order < o.order) {
			return -1;
		}
		return 0;
	}
}
