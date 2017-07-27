package org.xson.tangyuan.ognl.convert;

public interface IConverter {

	public boolean isSupportType(Object object);

	public Object convert(Object object, Class<?> targetClass);

}
