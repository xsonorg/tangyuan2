package org.xson.tangyuan.mapping;

public class DefaultMappingHandler implements MappingHandler {
	
	@Override
	public String columnToProperty(String column) {
		return column;
	}
}
