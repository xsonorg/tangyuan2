package org.xson.tangyuan.es.util;

import java.util.Collection;

import org.xson.common.object.XCO;

public class ESUtil {

	public static String arrayToString(Object obj) {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		Class<?> clazz = obj.getClass();
		if (int[].class == clazz) {
			int[] array = (int[]) obj;
			for (int i = 0; i < array.length; i++) {
				if (i > 0) {
					sb.append(",");
				}
				sb.append(array[i]);
			}
		} else if (long[].class == clazz) {
			long[] array = (long[]) obj;
			for (int i = 0; i < array.length; i++) {
				if (i > 0) {
					sb.append(",");
				}
				sb.append(array[i]);
			}
		} else if (float[].class == clazz) {
			float[] array = (float[]) obj;
			for (int i = 0; i < array.length; i++) {
				if (i > 0) {
					sb.append(",");
				}
				sb.append(array[i]);
			}
		} else if (double[].class == clazz) {
			double[] array = (double[]) obj;
			for (int i = 0; i < array.length; i++) {
				if (i > 0) {
					sb.append(",");
				}
				sb.append(array[i]);
			}
		} else if (short[].class == clazz) {
			short[] array = (short[]) obj;
			for (int i = 0; i < array.length; i++) {
				if (i > 0) {
					sb.append(",");
				}
				sb.append(array[i]);
			}
		} else if (byte[].class == clazz) {
			byte[] array = (byte[]) obj;
			for (int i = 0; i < array.length; i++) {
				if (i > 0) {
					sb.append(",");
				}
				sb.append(array[i]);
			}
		} else if (boolean[].class == clazz) {
			boolean[] array = (boolean[]) obj;
			for (int i = 0; i < array.length; i++) {
				if (i > 0) {
					sb.append(",");
				}
				sb.append(array[i]);
			}
		} else if (char[].class == clazz) {
			char[] array = (char[]) obj;
			for (int i = 0; i < array.length; i++) {
				if (i > 0) {
					sb.append(",");
				}
				sb.append('\'');
				sb.append(array[i]);
				sb.append('\'');
			}
		} else {
			Object[] array = (Object[]) obj;
			if (array.length > 0) {
				Class<?> itemClazz = array[0].getClass();
				if (XCO.class == itemClazz) {
					for (int i = 0; i < array.length; i++) {
						if (i > 0) {
							sb.append(",");
						}
						sb.append(((XCO) array[i]).toJSON());
					}
				} else {
					for (int i = 0; i < array.length; i++) {
						if (i > 0) {
							sb.append(",");
						}
						sb.append('\'');
						sb.append(array[i].toString());
						sb.append('\'');
					}
				}
			}
		}
		sb.append(']');
		return sb.toString();
	}

	public static String collectionToString(Collection<?> c) {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		int i = 0;
		for (Object item : c) {
			if (i++ > 0) {
				sb.append(",");
			}
			if (XCO.class == item.getClass()) {
				sb.append(((XCO) item).toJSON());
			} else {
				sb.append('\'');
				sb.append(item.toString());
				sb.append('\'');
			}
		}
		sb.append(']');
		return sb.toString();
	}

	public static String mergeURL(String host, String target) {
		if (target.startsWith("/")) {
			return host + target;
		}
		return host + "/" + target;
	}

}
