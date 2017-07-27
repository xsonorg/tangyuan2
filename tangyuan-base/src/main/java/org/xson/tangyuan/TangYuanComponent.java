package org.xson.tangyuan;

import java.util.Map;

public interface TangYuanComponent {

	void config(Map<String, String> properties);

	void start(String resource) throws Throwable;

	void stop(boolean wait);

}
