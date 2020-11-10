package org.xson.tangyuan.cache;

public abstract class AbstractReloaderCache extends AbstractCache {

	protected long reloadWaitTime = 1000L * 2L;

	//	protected InputStream getInputStreamForReload(String resource, String context, boolean placeholder, boolean useLocalStorage) throws Throwable {
	//		InputStream in = null;
	//		if (null != context) {
	//			in = new ByteArrayInputStream(context.getBytes(StandardCharsets.UTF_8));
	//		} else {
	//			in = MixedResourceManager.getInputStream(resource, placeholder, useLocalStorage);
	//		}
	//		return in;
	//	}
	//
	//	protected Properties getPropertiesForReload(String resource, String context, boolean placeholder, boolean useLocalStorage) throws Throwable {
	//		Properties p = null;
	//		if (null != context) {
	//			p = new Properties();
	//			p.load(new ByteArrayInputStream(context.getBytes(StandardCharsets.UTF_8)));
	//		} else {
	//			p = MixedResourceManager.getProperties(resource, placeholder, useLocalStorage);
	//		}
	//		return p;
	//	}
}
