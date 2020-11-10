package org.xson.tangyuan.es.converters;

public class JSONStringConverter extends AbstractEsResultConverter {

	public static String key = "@jsonString";

	@Override
	public Object convert(String json) {
		return json;
	}

}
