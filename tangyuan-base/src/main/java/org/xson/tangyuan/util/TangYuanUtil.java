package org.xson.tangyuan.util;

import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
			errorMessage = TangYuanContainer.getInstance().getErrorMessage();
		}
		result.setIntegerValue(TangYuanContainer.XCO_CODE_KEY, errorCode);
		result.setStringValue(TangYuanContainer.XCO_MESSAGE_KEY, errorMessage);
		return result;
	}

	public static ServiceException getServiceException(Throwable e) {
		return getServiceException(e, null);
	}

	public static ServiceException getServiceException(Throwable e, String message) {
		Throwable tx = e;
		if (e instanceof InvocationTargetException) {
			tx = ((InvocationTargetException) e).getTargetException();
		}
		if (tx instanceof ServiceException) {
			return (ServiceException) tx;
		}
		if (null != message) {
			return new ServiceException(message, tx);
		}
		return new ServiceException(tx);
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

	/**
	 * 转义正则特殊字符
	 * 
	 * '$', '(', ')', '*', '+', '.', '[', ']', '?', '\\', '^', '{', '}', '|'
	 * 
	 * @param regex
	 * @return
	 */
	public static String escapeRegexWord(String regex) {
		if (null == regex || 0 == regex.length()) {
			return null;
		}
		String[] fbsArr = { "\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|" };
		for (String key : fbsArr) {
			if (regex.contains(key)) {
				regex = regex.replace(key, "\\" + key);
			}
		}
		return regex;
	}

	public static String format(String str, Object... args) {
		if (null == args || 0 == args.length) {
			return str;
		}
		for (int i = 0; i < args.length; i++) {
			str = str.replaceFirst("\\{\\}", String.valueOf(args[i]));
		}
		return str;
	}

	public static String getHostIp() {
		InetAddress netAddress = null;
		try {
			netAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		if (null == netAddress) {
			return "unknown";
		}
		return netAddress.getHostAddress();
	}

	public static String getHostName() {
		InetAddress netAddress = null;
		try {
			netAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		if (null == netAddress) {
			return "unknown";
		}
		return netAddress.getHostName();
	}

	/**
	 * 是否是本地服务(a/b)
	 */
	public static boolean isLocalService(String serviceURI) {
		int len = serviceURI.length();
		int separator = 0;
		int brackets = 0;
		char chr = '0';
		for (int i = 0; i < len; i++) {
			chr = serviceURI.charAt(i);
			if ('/' == chr) {
				separator++;
			} else if ('{' == chr || '}' == chr) {
				brackets++;
			}
		}
		if (separator > 1) {
			return false;
		}
		if (brackets > 0) {
			return false;
		}
		return true;
	}

}
