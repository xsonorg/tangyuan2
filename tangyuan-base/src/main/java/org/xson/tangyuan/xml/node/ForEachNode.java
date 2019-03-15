package org.xson.tangyuan.xml.node;

import java.util.Collection;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.ognl.Ognl;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.xml.XmlParseException;

public abstract class ForEachNode implements TangYuanNode {

	private static int		indexMode0	= 0;
	private static int		indexMode1	= 1;
	private static int		indexMode2	= 2;
	private static int		indexMode3	= 3;
	private static int		indexMode4	= 4;
	private static int		indexMode5	= 5;

	protected TangYuanNode	sqlNode;

	/**
	 * 集合变量(从那个集合中遍历, 不能为空)
	 */
	protected Variable		collection;

	/**
	 * 集合中索引的变量名称
	 */
	protected String		index;

	protected String		open;

	protected String		close;

	protected String		separator;

	/** 开始索引, 可以是常量或者变量 */
	protected Object		start		= null;
	/** 结束索引 */
	protected Object		end			= null;
	/** 处理长度 */
	protected Object		pLen		= null;
	/** 索引模式[0:默认, 1:start, 2:end, 3:length, 4:start+end, 5:start+length] */
	protected int			indexMode	= 0;
	/** 忽略IndexOutOfBoundsException */
	protected boolean		ignoreIOOB	= false;

	@Override
	public boolean execute(ServiceContext context, Object arg) throws Throwable {
		int count = 0;
		Object obj = collection.getValue(arg);
		if (null == obj) {
			throw new TangYuanException("in the <foreach> node, the collection[" + collection.getOriginal() + "] is empty.");
		}
		append(context, open);

		if (null != obj && obj instanceof Collection) {
			count = foreachCollection(obj, context, arg);
		} else if (null != obj && obj.getClass().isArray()) {
			Class<?> clazz = obj.getClass();
			// if (int[].class == clazz) {
			// count = foreachIntArray(obj, context, arg);
			// } else if (long[].class == clazz) {
			// count = foreachLongArray(obj, context, arg);
			// } else if (float[].class == clazz) {
			// count = foreachFloatArray(obj, context, arg);
			// } else if (double[].class == clazz) {
			// count = foreachDoubleArray(obj, context, arg);
			// } else if (byte[].class == clazz) {
			// count = foreachByteArray(obj, context, arg);
			// } else if (short[].class == clazz) {
			// count = foreachShortArray(obj, context, arg);
			// } else if (boolean[].class == clazz) {
			// count = foreachBooleanArray(obj, context, arg);
			// } else if (char[].class == clazz) {
			// count = foreachCharArray(obj, context, arg);
			// } else {
			// count = foreachObjectArray(obj, context, arg);
			// }

			int arrayLength = 0;
			if (int[].class == clazz) {
				arrayLength = ((int[]) obj).length;
			} else if (long[].class == clazz) {
				arrayLength = ((long[]) obj).length;
			} else if (float[].class == clazz) {
				arrayLength = ((float[]) obj).length;
			} else if (double[].class == clazz) {
				arrayLength = ((double[]) obj).length;
			} else if (byte[].class == clazz) {
				arrayLength = ((byte[]) obj).length;
			} else if (short[].class == clazz) {
				arrayLength = ((short[]) obj).length;
			} else if (boolean[].class == clazz) {
				arrayLength = ((boolean[]) obj).length;
			} else if (char[].class == clazz) {
				arrayLength = ((char[]) obj).length;
			} else {
				arrayLength = ((Object[]) obj).length;
			}
			count = foreachArray(context, arg, arrayLength);
		} else {
			// 必须是集合类型
			throw new TangYuanException("Unsupported collection[" + collection.getOriginal() + "] type: " + obj.getClass().getName());
		}

		if (0 == count) {
			// 集合数量>0
			throw new TangYuanException("the number of elements that can be processed in the collection[" + collection.getOriginal() + "] is 0.");
		}

		append(context, close);
		return true;
	}

	protected abstract void append(ServiceContext context, String str);

	@SuppressWarnings("unused")
	private int foreachCollection(Object target, ServiceContext context, Object arg) throws Throwable {
		Collection<?> collection = (Collection<?>) target;
		if (this.indexMode == indexMode0) {
			int count = 0;
			for (Object item : collection) {
				// if (null == item) {
				// throw new TangYuanException(
				// "in the <foreach> node, there are empty elements in the collection[" + this.collection.getOriginal() + "].");
				// }
				// 对于用户的操作未知, 不能抛出异常
				Ognl.setValue(arg, index, count);
				if (count++ > 0) {
					append(context, separator);
				}
				sqlNode.execute(context, arg);
			}
			return count;
		}

		int iStart = getStartIndex(arg);
		int iEnd = getEndIndex(arg, iStart, collection.size());
		if (iStart >= iEnd) {
			return 0;
		}
		int i = 0;
		for (Object item : collection) {
			if (i >= iEnd) {
				break;
			}
			if (i++ < iStart) {
				continue;
			}
			Ognl.setValue(arg, index, i - 1);
			if (i > iStart) {
				append(context, separator);
			}
			sqlNode.execute(context, arg);
		}
		return iStart > iEnd ? 0 : iEnd - iStart;
	}

	private int foreachArray(ServiceContext context, Object arg, int arrayLength) throws Throwable {
		int iStart = 0;
		int iEnd = arrayLength;
		if (indexMode0 != this.indexMode) {
			iStart = getStartIndex(arg);
			iEnd = getEndIndex(arg, iStart, arrayLength);
		}
		for (int i = iStart; i < iEnd; i++) {
			Ognl.setValue(arg, index, i);
			if (i > iStart) {
				append(context, separator);
			}
			sqlNode.execute(context, arg);
		}
		return iStart > iEnd ? 0 : iEnd - iStart;
	}

	private int getIndexValue(Object arg, Object index, String msgPrefix) {
		try {
			Object val = index;
			if (index instanceof Variable) {
				val = ((Variable) index).getValue(arg);
			}
			return ((Integer) val).intValue();
		} catch (Throwable e) {
			throw new TangYuanException(msgPrefix + e.getMessage());
		}
	}

	private int getStartIndex(Object arg) {
		if (null == start) {
			return 0;
		}
		return getIndexValue(arg, this.start, "invalid start index: ");
	}

	private int getEndIndex(Object arg, int startIndex, int max) {
		int val = -1;
		if (null != this.end) {
			val = getIndexValue(arg, this.end, "invalid end index: ");
		} else if (null != this.pLen) {
			val = getIndexValue(arg, this.pLen, "invalid length: ");
			val = startIndex + val;
		} else {
			val = max;
		}

		if (val <= max) {
			return val;
		}
		if (ignoreIOOB) {
			return max;
		}

		throw new TangYuanException("Index out of bounds: " + val);
	}

	public static int getAndCheckIndexMode(Object start, Object end, Object pLen) {
		if (null == start && null == end && null == pLen) {
			return indexMode0;
		}
		if (null != start && null == end && null == pLen) {
			return indexMode1;
		}
		if (null == start && null != end && null == pLen) {
			return indexMode2;
		}
		if (null == start && null == end && null != pLen) {
			return indexMode3;
		}
		if (null != start && null != end && null == pLen) {
			return indexMode4;
		}
		if (null != start && null == end && null != pLen) {
			return indexMode5;
		}
		throw new XmlParseException("in the <forEach> node, meaningless attribute 'end' or 'len'.");
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	// /**
	// * @param iterate
	// * true: 遍历模式
	// */
	// private int foreachObjectArray(Object target, ServiceContext context, Object arg) throws Throwable {
	// Object[] array = (Object[]) target;
	//
	// if (this.indexMode == indexMode0) {
	// int count = 0;
	// for (int i = 0; i < array.length; i++) {
	// Ognl.setValue(arg, index, count);
	// if (count++ > 0) {
	// append(context, separator);
	// }
	// sqlNode.execute(context, arg);
	// }
	// return count;
	// }
	//
	// int iStart = getStartIndex(arg);
	// int iEnd = getEndIndex(arg, array.length);
	// int count = 0;
	// for (int i = iStart; i < iEnd; i++) {
	// Ognl.setValue(arg, index, count);
	// if (count++ > 0) {
	// append(context, separator);
	// }
	// sqlNode.execute(context, arg);
	// }
	// return iStart > iEnd ? 0 : iEnd - iStart;
	// }
	//
	// private int foreachIntArray(Object target, ServiceContext context, Object arg) throws Throwable {
	// int[] array = (int[]) target;
	// if (this.indexMode == indexMode0) {
	// for (int i = 0; i < array.length; i++) {
	// Ognl.setValue(arg, index, i);
	// if (i > 0) {
	// append(context, separator);
	// }
	// sqlNode.execute(context, arg);
	// }
	// return array.length;
	// }
	//
	// int iStart = getStartIndex(arg);
	// int iEnd = getEndIndex(arg, array.length);
	// for (int i = iStart; i < iEnd; i++) {
	// Ognl.setValue(arg, index, i);
	// if (i > 0) {
	// append(context, separator);
	// }
	// sqlNode.execute(context, arg);
	// }
	// return iStart > iEnd ? 0 : iEnd - iStart;
	// }
	//
	// private int foreachLongArray(Object target, ServiceContext context, Object arg) throws Throwable {
	// long[] array = (long[]) target;
	//
	// if (this.indexMode == indexMode0) {
	// for (int i = 0; i < array.length; i++) {
	// Ognl.setValue(arg, index, i);
	// if (i > 0) {
	// append(context, separator);
	// }
	// sqlNode.execute(context, arg);
	// }
	// return array.length;
	// }
	//
	// int iStart = getStartIndex(arg);
	// int iEnd = getEndIndex(arg, array.length);
	// for (int i = iStart; i < iEnd; i++) {
	// Ognl.setValue(arg, index, i);
	// if (i > 0) {
	// append(context, separator);
	// }
	// sqlNode.execute(context, arg);
	// }
	// return iStart > iEnd ? 0 : iEnd - iStart;
	// }
	//
	// private int foreachBooleanArray(Object target, ServiceContext context, Object arg) throws Throwable {
	// boolean[] array = (boolean[]) target;
	//
	// if (this.indexMode == indexMode0) {
	// for (int i = 0; i < array.length; i++) {
	// Ognl.setValue(arg, index, i);
	// if (i > 0) {
	// append(context, separator);
	// }
	// sqlNode.execute(context, arg);
	// }
	// return array.length;
	// }
	//
	// int iStart = getStartIndex(arg);
	// int iEnd = getEndIndex(arg, array.length);
	// for (int i = iStart; i < iEnd; i++) {
	// Ognl.setValue(arg, index, i);
	// if (i > 0) {
	// append(context, separator);
	// }
	// sqlNode.execute(context, arg);
	// }
	// return iStart > iEnd ? 0 : iEnd - iStart;
	// }
	//
	// private int foreachByteArray(Object target, ServiceContext context, Object arg) throws Throwable {
	// byte[] array = (byte[]) target;
	//
	// if (this.indexMode == indexMode0) {
	// for (int i = 0; i < array.length; i++) {
	// Ognl.setValue(arg, index, i);
	// if (i > 0) {
	// append(context, separator);
	// }
	// sqlNode.execute(context, arg);
	// }
	// return array.length;
	// }
	//
	// int iStart = getStartIndex(arg);
	// int iEnd = getEndIndex(arg, array.length);
	// for (int i = iStart; i < iEnd; i++) {
	// Ognl.setValue(arg, index, i);
	// if (i > 0) {
	// append(context, separator);
	// }
	// sqlNode.execute(context, arg);
	// }
	// return iStart > iEnd ? 0 : iEnd - iStart;
	// }
	//
	// private int foreachCharArray(Object target, ServiceContext context, Object arg) throws Throwable {
	// char[] array = (char[]) target;
	//
	// if (this.indexMode == indexMode0) {
	// for (int i = 0; i < array.length; i++) {
	// Ognl.setValue(arg, index, i);
	// if (i > 0) {
	// append(context, separator);
	// }
	// sqlNode.execute(context, arg);
	// }
	// return array.length;
	// }
	//
	// int iStart = getStartIndex(arg);
	// int iEnd = getEndIndex(arg, array.length);
	// for (int i = iStart; i < iEnd; i++) {
	// Ognl.setValue(arg, index, i);
	// if (i > 0) {
	// append(context, separator);
	// }
	// sqlNode.execute(context, arg);
	// }
	// return iStart > iEnd ? 0 : iEnd - iStart;
	// }
	//
	// private int foreachDoubleArray(Object target, ServiceContext context, Object arg) throws Throwable {
	// double[] array = (double[]) target;
	//
	// if (this.indexMode == indexMode0) {
	// for (int i = 0; i < array.length; i++) {
	// Ognl.setValue(arg, index, i);
	// if (i > 0) {
	// append(context, separator);
	// }
	// sqlNode.execute(context, arg);
	// }
	// return array.length;
	// }
	//
	// int iStart = getStartIndex(arg);
	// int iEnd = getEndIndex(arg, array.length);
	// for (int i = iStart; i < iEnd; i++) {
	// Ognl.setValue(arg, index, i);
	// if (i > 0) {
	// append(context, separator);
	// }
	// sqlNode.execute(context, arg);
	// }
	// return iStart > iEnd ? 0 : iEnd - iStart;
	// }
	//
	// private int foreachFloatArray(Object target, ServiceContext context, Object arg) throws Throwable {
	// float[] array = (float[]) target;
	//
	// if (this.indexMode == indexMode0) {
	// for (int i = 0; i < array.length; i++) {
	// Ognl.setValue(arg, index, i);
	// if (i > 0) {
	// append(context, separator);
	// }
	// sqlNode.execute(context, arg);
	// }
	// return array.length;
	// }
	//
	// int iStart = getStartIndex(arg);
	// int iEnd = getEndIndex(arg, array.length);
	// for (int i = iStart; i < iEnd; i++) {
	// Ognl.setValue(arg, index, i);
	// if (i > 0) {
	// append(context, separator);
	// }
	// sqlNode.execute(context, arg);
	// }
	// return iStart > iEnd ? 0 : iEnd - iStart;
	//
	// }
	//
	// private int foreachShortArray(Object target, ServiceContext context, Object arg) throws Throwable {
	// short[] array = (short[]) target;
	//
	// if (this.indexMode == indexMode0) {
	// for (int i = 0; i < array.length; i++) {
	// Ognl.setValue(arg, index, i);
	// if (i > 0) {
	// append(context, separator);
	// }
	// sqlNode.execute(context, arg);
	// }
	// return array.length;
	// }
	//
	// int iStart = getStartIndex(arg);
	// int iEnd = getEndIndex(arg, array.length);
	// for (int i = iStart; i < iEnd; i++) {
	// Ognl.setValue(arg, index, i);
	// if (i > 0) {
	// append(context, separator);
	// }
	// sqlNode.execute(context, arg);
	// }
	// return iStart > iEnd ? 0 : iEnd - iStart;
	// }

	///////////////////////////////////////////////////////////////////////////////////////////
}
