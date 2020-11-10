package org.xson.tangyuan.log.ext;

import org.xson.tangyuan.log.LogFactory;

/**
 * 日志扩展功能工具类
 */
public class LogExtUtil {

	private static LogExtVo getLogExtVo() {
		LogExt ext = LogFactory.getLogExt();
		if (null != ext) {
			return ext.getExtVo();
		}
		return null;
	}

	public static boolean isWebRequestHeaderPrint() {
		LogExtVo vo = getLogExtVo();
		if (null != vo) {
			return vo.isWebRequestHeaderPrint();
		}
		return false;
	}

	public static boolean isSqlErrorLogPrint() {
		LogExtVo vo = getLogExtVo();
		if (null != vo) {
			return vo.isSqlErrorLogPrint();
		}
		return false;
	}

	public static boolean isWebResponseResultPrint() {
		LogExtVo vo = getLogExtVo();
		if (null != vo) {
			return vo.isWebResponseResultPrint();
		}
		return false;
	}

	public static boolean isEsResponseResultPrint() {
		LogExtVo vo = getLogExtVo();
		if (null != vo) {
			return vo.isEsResponseResultPrint();
		}
		return false;
	}

	public static boolean isEsResponseResultErrorPrint() {
		LogExtVo vo = getLogExtVo();
		if (null != vo) {
			return vo.isEsResponseResultErrorPrint();
		}
		return false;
	}

	public static boolean isMongoSqlPrint() {
		LogExtVo vo = getLogExtVo();
		if (null != vo) {
			return vo.isMongoSqlPrint();
		}
		return false;
	}

	public static boolean isMongoSqlShellPrint() {
		LogExtVo vo = getLogExtVo();
		if (null != vo) {
			return vo.isMongoSqlShellPrint();
		}
		return false;
	}

	public static boolean isMongoErrorLogPrint() {
		LogExtVo vo = getLogExtVo();
		if (null != vo) {
			return vo.isMongoErrorLogPrint();
		}
		return false;
	}

	//	public static boolean isMongoShellPrint() {
	//		LogExtVo vo = getLogExtVo();
	//		if (null != vo) {
	//			return vo.isMongoShellPrint();
	//		}
	//		return false;
	//	}
}
