package org.xson.tangyuan.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Vector;

/**
 * Java中的切片工具，支持字符串，数组，集合
 */
public class SliceUtil {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Collection<Object> createCollection(Collection<?> src) {
		Class<?> type = src.getClass();
		Collection collection = null;
		if (type == ArrayList.class) {
			collection = new ArrayList<Object>();
		} else if (type == LinkedList.class) {
			collection = new LinkedList<Object>();
		} else if (type == Vector.class) {
			collection = new Vector<Object>();
		} else if (type == Stack.class) {
			collection = new Stack<Object>();
		} else if (type == HashSet.class) {
			collection = new HashSet<Object>();
		} else if (type == TreeSet.class) {
			collection = new TreeSet<Object>();
		} else if (type == LinkedHashSet.class) {
			collection = new LinkedHashSet<Object>();
		} else if (type.isAssignableFrom(EnumSet.class)) {
			collection = EnumSet.noneOf((Class<Enum>) type);
		} else {
			try {
				collection = (Collection) type.newInstance();
			} catch (Exception e) {
				throw new RuntimeException("create collection instane error, class " + type.getName());
			}
		}
		return collection;
	}

	@SuppressWarnings({ "unchecked" })
	private static <T> T[] createObjectArray(Class<T> type, int sz) {
		return (T[]) Array.newInstance(type, sz);
	}

	/**
	 * 切片参数解析
	 */
	private static Integer[] parse(int srcLength, String arg) {
		int length = arg.length();

		Integer[] type = { null, null, null };
		int idx = 0;
		int start = 0;
		for (int i = 0; i < length; i++) {
			char key = arg.charAt(i);
			if (':' == key) {
				if (i == start) {
					type[idx] = null;
				} else {
					type[idx] = Integer.parseInt(arg.substring(start, i).trim());
				}
				start = i + 1;
				idx++;
				if (idx == type.length) {
					throw new IllegalArgumentException("Invalid parameter: " + arg);
				}
			}
		}

		if (start != length) {
			type[idx] = Integer.parseInt(arg.substring(start, length).trim());
		}

		if (null == type[2]) {
			type[2] = 1;
		}

		if (type[2].intValue() < 0) {
			// 逆向
			if (null == type[0]) {
				type[0] = -1;
			}
			if (null == type[1]) {
				type[1] = -srcLength - 1;
			}
		} else if (type[2].intValue() > 0) {
			// 正向
			if (null == type[0]) {
				type[0] = 0;
			}
			if (null == type[1]) {
				type[1] = srcLength;
			}
		}

		if (0 == type[2].intValue()) {
			throw new IllegalArgumentException("Invalid parameter: " + arg);
		}

		return type;
	}

	/**
	 * 索引是否越界 -len(sequence)<=index<=len(sequence)-1
	 */
	private static boolean isOutBounds(Integer index, int length) {
		if (null == index) {
			return false;
		}
		int idx = index.intValue();
		return idx < -length || idx > length - 1;
	}

	/**
	 * 字符串切片
	 * 
	 * @param src
	 *            被切片的字符串
	 * @param arg
	 *            切片参数
	 * @return 切片后的字符串
	 */
	public static String slice(String src, String arg) {
		if (null == src || 0 == src.length()) {
			return src;
		}
		Integer[] type = parse(src.length(), arg);
		int length = src.length();
		if (isOutBounds(type[0], length)) {
			new IndexOutOfBoundsException("Index out of bounds:" + arg);
		}
		if (isOutBounds(type[1], length)) {
			new IndexOutOfBoundsException("Index out of bounds:" + arg);
		}

		int start = type[0].intValue();
		int end = type[1].intValue();
		// 步长
		int step = 1;
		// 正向
		boolean positive = true;
		if (type[2] > 0) {
			positive = true;
			step = type[2];
			if (start < 0) {
				start = length + start;
			}
			if (end < 0) {
				end = length + end;
			}
			if (start >= end) {
				return "";
			}
		} else {
			positive = false;
			step = -type[2];
			start = length + start;
			end = length + end;
			if (start <= end) {
				return "";
			}
		}

		if (positive) {
			// 正向
			if (1 == step) {
				return src.substring(start, end);
			} else {
				StringBuilder sb = new StringBuilder();
				for (int i = start; i < end; i += step) {
					sb.append(src.charAt(i));
				}
				return sb.toString();
			}
		} else {
			// 逆向
			StringBuilder sb = new StringBuilder();
			for (int i = start; i > end; i -= step) {
				sb.append(src.charAt(i));
			}
			return sb.toString();
		}
	}

	/**
	 * 集合切片
	 * 
	 * @param src
	 *            被切片的集合
	 * @param arg
	 *            切片参数
	 * @return 切片后的集合
	 */
	public static Collection<?> slice(Collection<?> src, String arg) {
		if (null == src || 0 == src.size()) {
			return src;
		}
		int length = src.size();
		Integer[] type = parse(length, arg);

		if (isOutBounds(type[0], length)) {
			new IndexOutOfBoundsException("Index out of bounds:" + arg);
		}
		if (isOutBounds(type[1], length)) {
			new IndexOutOfBoundsException("Index out of bounds:" + arg);
		}

		Collection<Object> result = createCollection(src);

		int start = type[0].intValue();
		int end = type[1].intValue();
		int step = 1;
		boolean positive = true;
		if (type[2] > 0) {
			positive = true;
			step = type[2];
			if (start < 0) {
				start = length + start;
			}
			if (end < 0) {
				end = length + end;
			}
			if (start >= end) {
				return result;
			}
		} else {
			positive = false;
			step = -type[2];
			start = length + start;
			end = length + end;
			if (start <= end) {
				return result;
			}
		}

		Object[] array = src.toArray();

		if (positive) {
			for (int i = start; i < end; i += step) {
				result.add(array[i]);
			}
		} else {
			for (int i = start; i > end; i -= step) {
				result.add(array[i]);
			}
		}
		return result;
	}

	/**
	 * 对象数组切片
	 * 
	 * @param src
	 *            被切片的对象数组
	 * @param arg
	 *            切片参数
	 * @return 切片后的对象数组
	 */
	@SuppressWarnings({ "unchecked" })
	public static <T> T[] slice(T[] src, String arg) {
		if (null == src || 0 == src.length) {
			return src;
		}

		int length = src.length;
		Integer[] type = parse(length, arg);

		if (isOutBounds(type[0], length)) {
			new IndexOutOfBoundsException("Index out of bounds:" + arg);
		}
		if (isOutBounds(type[1], length)) {
			new IndexOutOfBoundsException("Index out of bounds:" + arg);
		}

		T[] array = (T[]) createObjectArray(src[0].getClass(), 0);
		int start = type[0].intValue();
		int end = type[1].intValue();
		int step = 1;
		boolean positive = true;
		if (type[2] > 0) {
			positive = true;
			step = type[2];
			if (start < 0) {
				start = length + start;
			}
			if (end < 0) {
				end = length + end;
			}
			if (start >= end) {
				return array;
			}
		} else {
			positive = false;
			step = -type[2];
			start = length + start;
			end = length + end;
			if (start <= end) {
				return array;
			}
		}

		List<T> list = new ArrayList<T>();
		if (positive) {
			for (int i = start; i < end; i += step) {
				list.add(src[i]);
			}
		} else {
			for (int i = start; i > end; i -= step) {
				list.add(src[i]);
			}
		}
		return list.toArray(array);
	}

}
