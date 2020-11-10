package org.xson.tangyuan.es.converters;

public class JSONConverter extends AbstractEsResultConverter {

	public static String key = "@json";

	@Override
	public Object convert(String json) {
		return json;
	}

}
