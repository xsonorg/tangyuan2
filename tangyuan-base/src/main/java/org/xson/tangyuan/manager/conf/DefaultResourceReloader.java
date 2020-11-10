package org.xson.tangyuan.manager.conf;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.xson.tangyuan.app.AppPlaceholder;
import org.xson.tangyuan.util.MixedResourceManager;
import org.xson.tangyuan.util.PlaceholderResourceSupport;

public abstract class DefaultResourceReloader implements ResourceReloader {

	//	@Override
	//	public void reload(String resource, String context) throws Throwable {
	//	}
	//	@Override
	//	public void reload(String resource) throws Throwable {
	//	}

	protected InputStream getInputStreamForReload(String resource, String context, boolean placeholder, boolean useLocalStorage) throws Throwable {
		InputStream in = null;
		if (null != context) {
			in = new ByteArrayInputStream(context.getBytes(StandardCharsets.UTF_8));
			if (placeholder) {
				in = PlaceholderResourceSupport.processInputStream(in, AppPlaceholder.getData());
			}
		} else {
			in = MixedResourceManager.getInputStream(resource, placeholder, useLocalStorage);
		}
		return in;
	}

	protected Properties getPropertiesForReload(String resource, String context, boolean placeholder, boolean useLocalStorage) throws Throwable {
		Properties p = null;
		if (null != context) {
			p = new Properties();
			InputStream in = null;
			if (placeholder) {
				in = PlaceholderResourceSupport.processInputStream(new ByteArrayInputStream(context.getBytes(StandardCharsets.UTF_8)), AppPlaceholder.getData());
			} else {
				in = new ByteArrayInputStream(context.getBytes(StandardCharsets.UTF_8));
			}
			p.load(in);
			in.close();
		} else {
			p = MixedResourceManager.getProperties(resource, placeholder, useLocalStorage);
		}
		return p;
	}

}
