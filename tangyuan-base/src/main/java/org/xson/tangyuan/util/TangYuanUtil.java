package org.xson.tangyuan.util;

import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.executor.ServiceException;

public class TangYuanUtil {

	/** 获取完整的服务名 */
	public static String getQualifiedName(String ns, String id, String ver, String separator) {
		String qName = id;
		if (null != ns && ns.length() > 0) {
			qName = ns + separator + qName;
		}
		if (null != ver && ver.length() > 0) {
			qName = qName + separator + ver;
		}
		return qName;
	}

	public static XCO retObjToXco(Object obj) {
		return retObjToXco(obj, TangYuanContainer.SUCCESS_CODE);
	}

	/** 第一版 */
	// public static XCO retObjToXco(Object obj, int code) {
	// XCO result = null;
	// if (null == obj) {
	// result = new XCO();
	// } else {
	// if (obj instanceof XCO) {
	// result = (XCO) obj;
	// } else {
	// result = new XCO();
	// result.setObjectValue(TangYuanContainer.XCO_DATA_KEY, obj);
	// result.setIntegerValue(TangYuanContainer.XCO_PACKAGE_KEY, TangYuanContainer.SUCCESS_CODE);
	// }
	// }
	// if (null == result.getCode()) {
	// result.setIntegerValue(TangYuanContainer.XCO_CODE_KEY, code);
	// }
	// return result;
	// }

	public static XCO retObjToXco(Object obj, int code) {
		if ((null != obj) && (obj instanceof XCO) && (((XCO) obj).exists(TangYuanContainer.XCO_CODE_KEY))) {
			return (XCO) obj;
		}
		XCO result = new XCO();
		result.setIntegerValue(TangYuanContainer.XCO_CODE_KEY, code);
		if (null != obj) {
			result.setObjectValue(TangYuanContainer.XCO_DATA_KEY, obj);
		}
		result.setIntegerValue(TangYuanContainer.XCO_PACKAGE_KEY, TangYuanContainer.SUCCESS_CODE);
		return result;
	}

	public static Object getRealData(Object obj) {
		if (null == obj) {
			return null;
		}
		if (obj instanceof XCO) {
			XCO xco = (XCO) obj;
			if (xco.exists(TangYuanContainer.XCO_PACKAGE_KEY)) {
				return xco.getData();
			}
			return xco;
		}
		return obj;
	}

	public static XCO getExceptionResult(Throwable e) {
		XCO result = new XCO();
		Throwable tx = e;
		if (e instanceof InvocationTargetException) {
			tx = ((InvocationTargetException) e).getTargetException();
		}
		int errorCode = 0;
		String errorMessage = null;
		if (tx instanceof ServiceException) {
			ServiceException ex = (ServiceException) tx;
			errorCode = ex.getErrorCode();
			errorMessage = ex.getErrorMessage();
		} else {
			errorCode = TangYuanContainer.getInstance().getErrorCode();
			// errorMessage = e.getMessage();
			errorMessage = TangYuanContainer.getInstance().getErrorMessage();
		}
		result.setIntegerValue(TangYuanContainer.XCO_CODE_KEY, errorCode);
		result.setStringValue(TangYuanContainer.XCO_MESSAGE_KEY, errorMessage);
		return result;
	}

	public static <T> T newInstance(Class<T> clazz) {
		return newInstance(clazz, TangYuanContainer.getInstance().isJdkProxy());
	}

	@SuppressWarnings("unchecked")
	public static <T> T newInstance(Class<T> clazz, boolean jdkProxy) {
		try {
			if (jdkProxy) {
				return clazz.newInstance();
			}
			return (T) new CglibProxy().getProxy(clazz);
		} catch (Throwable e) {
			throw new TangYuanException(e);
		}
	}

	public static boolean isHost(String str) {
		return isIp(str) ? true : (isDomain(str) ? true : false);
	}

	public static boolean isIp(String ip) {
		String regex = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(ip);
		return matcher.matches();
	}

	public static boolean isDomain(String domain) {
		String regex = "^([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,10}$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(domain);
		return matcher.matches();
	}

}
