package org.xson.tangyuan.ognl.convert;

import org.xson.common.object.XCO;

/**
 * @deprecated
 */
public class XmlStringConverter implements IConverter {

	@Override
	public boolean isSupportType(Object object) {
		if (object instanceof String) {
			String xml = (String) object;
			if (xml.startsWith("<?xml version") && xml.endsWith(">")) {
				return true;
			}
		}
		return false;
	}

	@Override
	public XCO convert(Object object, Class<?> targetClass) {
		return XCO.fromXML((String) object);
	}
}
