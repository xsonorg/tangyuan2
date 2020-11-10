package org.xson.tangyuan.log.log4j;

import java.io.InputStream;

import org.apache.log4j.PropertyConfigurator;
import org.xson.tangyuan.log.LogLoader;
import org.xson.tangyuan.util.MixedResourceManager;

public class Log4jLoader implements LogLoader {

	@Override
	public void load(String resource) throws Throwable {
		InputStream in = MixedResourceManager.getInputStream(resource, false, true);
		PropertyConfigurator.configure(in);
		in.close();
	}

	@Override
	public void reload(String resource) throws Throwable {
		load(resource);
	}

}
