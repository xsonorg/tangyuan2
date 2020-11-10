package org.xson.tangyuan.rpc.host;

import java.net.URI;
import java.util.Map;
import java.util.Properties;

import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.manager.conf.DefaultResourceReloader;
import org.xson.tangyuan.util.CollectionUtils;

/**
 * 服务域名/IP管理
 */
public class RemoteHostManager extends DefaultResourceReloader {

	private Log                 log     = LogFactory.getLog(getClass());

	private Map<String, String> hostMap = null;

	/**
	 * 解析URL<br />
	 * 
	 * http://xxx.xson.org:8080/a/b	<br />
	 * http://xxx.xson.org:88/a/b	<br />
	 * http://xxx.xson.org:80/a/b	<br />
	 */
	public Object parse(String url, String type) throws Throwable {
		if (CollectionUtils.isEmpty(hostMap)) {
			return url;
		}
		// 后期可以支持TCP
		URI    uri       = new URI(url);
		String domain    = uri.getHost();
		String newDomain = this.hostMap.get(domain);
		if (null == newDomain) {
			return url;
		}
		// 对端口的兼容
		if (uri.getPort() != -1) {
			domain = domain + ":" + uri.getPort();
		}
		int pos = url.indexOf(domain);
		url = url.substring(0, pos) + newDomain + url.substring(pos + domain.length());
		return url;
	}

	public void init(String resource) throws Throwable {
		reload0(resource);
	}

	private void update(Map<String, String> hostMap) {
		this.hostMap = hostMap;
	}

	@Override
	public void reload(String resource) throws Throwable {
		try {
			reload0(resource);
			log.info(TangYuanLang.get("resource.reload"), resource);
		} catch (Throwable e) {
			log.error(e);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void reload0(String resource) throws Throwable {
		Properties p = getPropertiesForReload(resource, null, true, true);
		if (null != p) {
			update((Map) p);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//		Properties p = null;
	//		try {
	//			p = getPropertiesForReload(resource, null, true, true);
	//		} catch (Throwable e) {
	//		}
	//		if (null != p) {
	//			update((Map) p);
	//		}
	//	private RemoteHostVo vo = null;
	//	public String parse(String url, URI uri) throws Throwable {
	//		if (null == this.hostMap) {
	//			return url;
	//		}
	//		if (null == uri) {
	//			uri = new URI(url);
	//		}
	//		String domain    = uri.getHost();
	//		String newDomain = this.hostMap.get(domain);
	//		if (null == newDomain) {
	//			return url;
	//		}
	//
	//		int pos = url.indexOf(domain);
	//		url = url.substring(0, pos) + newDomain + url.substring(pos + domain.length());
	//		return url;
	//	}
}
