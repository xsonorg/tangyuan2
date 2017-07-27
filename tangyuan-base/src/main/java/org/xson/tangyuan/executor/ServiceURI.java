package org.xson.tangyuan.executor;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.util.TangYuanUtil;

// service+mode
// http://org.xson/x/y/v/z
// tcp://org.xson/x/y/v/z
// x:service
// y:method
// v:版本
// z:标识,扩展，表示服务从哪里获取

public class ServiceURI {

	private String	original;
	private String	host;
	private String	ns;
	private String	service;
	private String	version;
	private String	mark;

	public String getNs() {
		return ns;
	}

	public String getService() {
		return service;
	}

	public String getVersion() {
		return version;
	}

	public String getMark() {
		return mark;
	}

	public String getHost() {
		return host;
	}

	public String getOriginal() {
		return original;
	}

	public String getQualifiedServiceName() {
		return TangYuanUtil.getQualifiedName(ns, service, version, TangYuanContainer.getInstance().getNsSeparator());
	}

	@Override
	public String toString() {
		return original;
	}

	// private static Map<String, Integer> hostMap = new ConcurrentHashMap<String, Integer>();
	// public static ServiceURI parse(String url) throws URISyntaxException {
	// if (null == url) {
	// throw new TangYuanException("url is null.");
	// }
	// url = url.trim();
	// URI uri = new URI(url);// cache
	// ServiceURI sURI = new ServiceURI();
	//
	// sURI.original = url;
	// sURI.host = uri.getHost();
	// String path = uri.getPath();
	// if (null == path || path.length() < 3) {
	// throw new TangYuanException("Invalid service path: " + url);
	// }
	// if (path.startsWith("/")) {
	// path = path.substring(1);
	// }
	//
	// String[] array = path.split("/");
	//
	// if (null == sURI.host && array.length > 0) {
	// boolean includeHost = false;
	// if (hostMap.containsKey(array[0])) {
	// includeHost = true;
	// } else if (TangYuanUtil.isHost(array[0])) {
	// includeHost = true;
	// hostMap.put(array[0], 1);
	// }
	// if (includeHost) {
	// sURI.host = array[0];
	// String[] temp = new String[array.length - 1];
	// System.arraycopy(array, 1, temp, 0, array.length - 1);
	// array = temp;
	// }
	// }
	//
	// if (array.length == 1) {
	// sURI.service = array[0];
	// } else if (array.length == 2) {
	// sURI.ns = array[0];
	// sURI.service = array[1];
	// } else if (array.length == 3) {
	// sURI.ns = array[0];
	// sURI.service = array[1];
	// sURI.version = array[2];
	// } else if (array.length == 4) {
	// sURI.ns = array[0];
	// sURI.service = array[1];
	// sURI.version = array[2];
	// sURI.mark = array[3];
	// } else {
	// throw new TangYuanException("Invalid service path: " + url);
	// }
	// return sURI;
	// }

	public static ServiceURI parseUrlPath(String urlPath) {
		if (null == urlPath) {
			throw new TangYuanException("The url path can not be empty.");
		}
		ServiceURI sURI = new ServiceURI();
		sURI.original = urlPath;
		String path = urlPath.trim();
		if (null == path || path.length() < 3) {
			throw new TangYuanException("Invalid service path: " + urlPath);
		}
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		String[] array = path.split("/");
		if (array.length == 1) {
			sURI.service = array[0];
		} else if (array.length == 2) {
			sURI.ns = array[0];
			sURI.service = array[1];
		} else if (array.length == 3) {
			sURI.ns = array[0];
			sURI.service = array[1];
			sURI.version = array[2];
		} else if (array.length == 4) {
			sURI.ns = array[0];
			sURI.service = array[1];
			sURI.version = array[2];
			sURI.mark = array[3];
		} else {
			throw new TangYuanException("Invalid service path: " + urlPath);
		}

		if ("@".equals(sURI.version) || "#".equals(sURI.version)) {
			sURI.version = null;
		}

		return sURI;
	}

}
