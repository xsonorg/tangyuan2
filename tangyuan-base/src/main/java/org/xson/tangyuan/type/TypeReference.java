package org.xson.tangyuan.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class TypeReference<T> {

	private final Type rawType;

	protected TypeReference() {
		rawType = getSuperclassTypeParameter(getClass());
	}

	Type getSuperclassTypeParameter(Class<?> clazz) {
		Type genericSuperclass = clazz.getGenericSuperclass();
		if (genericSuperclass instanceof Class) {
			// try to climb up the hierarchy until meet something useful
			if (TypeReference.class != genericSuperclass) {
				return getSuperclassTypeParameter(clazz.getSuperclass());
			}

			throw new TypeException("'" + getClass() + "' extends TypeReference but misses the type parameter. "
					+ "Remove the extension or add a type parameter to it.");
		}

		Type rawType = ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
		// remove this when Reflector is fixed to return Types
		if (rawType instanceof ParameterizedType) {
			rawType = ((ParameterizedType) rawType).getRawType();
		}

		return rawType;
	}

	public final Type getRawType() {
		return rawType;
	}

	@Override
	public String toString() {
		return rawType.toString();
	}

}
