package org.xson.tangyuan.ognl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class FieldVo implements Comparable<FieldVo> {

	public static Map<Class<?>, FieldVoWrapper>	fieldVoWrapperCache	= new HashMap<Class<?>, FieldVoWrapper>(32);

	private String								name;
	private Method								getter;
	private Method								setter;

	public FieldVo(Field field, Method getter, Method setter, Class<?> clazz) {
		this.name = field.getName();
		this.getter = getter;
		this.getter.setAccessible(true);
		this.setter = setter;
		this.setter.setAccessible(true);
	}

	public String getName() {
		return name;
	}

	public Method getGetter() {
		return getter;
	}

	public Method getSetter() {
		return setter;
	}

	@Override
	public int compareTo(FieldVo o) {
		return this.name.compareTo(o.name);
	}

}
