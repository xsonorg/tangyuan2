package org.xson.tangyuan.xml.node;

import java.util.Collection;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.ognl.Ognl;
import org.xson.tangyuan.ognl.vars.Variable;

public abstract class ForEachNode implements TangYuanNode {

	protected TangYuanNode	sqlNode;

	/**
	 * 集合变量, 从那个集合中遍历, 可为空,但是此种情况下必须有sqlNode
	 */
	protected Variable		collection;

	/**
	 * 集合中索引的变量名称
	 */
	protected String		index;

	protected String		open;

	protected String		close;

	protected String		separator;

	@Override
	public boolean execute(ServiceContext context, Object arg) throws Throwable {
		int count = 0;
		Object obj = collection.getValue(arg);
		append(context, open);

		if (null != obj && obj instanceof Collection) {
			count = foreachCollection(obj, context, arg);
		} else if (null != obj && obj.getClass().isArray()) {
			Class<?> clazz = obj.getClass();
			if (int[].class == clazz) {
				count = foreachIntArray(obj, context, arg);
			} else if (long[].class == clazz) {
				count = foreachLongArray(obj, context, arg);
			} else if (float[].class == clazz) {
				count = foreachFloatArray(obj, context, arg);
			} else if (double[].class == clazz) {
				count = foreachDoubleArray(obj, context, arg);
			} else if (byte[].class == clazz) {
				count = foreachByteArray(obj, context, arg);
			} else if (short[].class == clazz) {
				count = foreachShortArray(obj, context, arg);
			} else if (boolean[].class == clazz) {
				count = foreachBooleanArray(obj, context, arg);
			} else if (char[].class == clazz) {
				count = foreachCharArray(obj, context, arg);
			} else {
				count = foreachObjectArray(obj, context, arg);
			}
		} else {
			throw new TangYuanException("ForEachNode: 获取对象非集合获取集合元素为空");
		}

		if (0 == count) {
			throw new TangYuanException("ForEachNode: 获取对象非集合获取集合元素为空");
		}

		append(context, close);
		return true;
	}

	protected abstract void append(ServiceContext context, String str);

	private int foreachCollection(Object target, ServiceContext context, Object arg) throws Throwable {
		Collection<?> collection = (Collection<?>) target;
		int count = 0;
		for (Object item : collection) {
			if (null == item) {
				throw new TangYuanException("collection element[" + count + "] is null");// TODO
			}
			Ognl.setValue(arg, index, count);
			if (count++ > 0) {
				append(context, separator);
			}
			sqlNode.execute(context, arg);
		}
		return count;
	}

	/**
	 * @param iterate
	 *            true: 遍历模式
	 */
	private int foreachObjectArray(Object target, ServiceContext context, Object arg) throws Throwable {
		Object[] array = (Object[]) target;
		int count = 0;
		for (int i = 0; i < array.length; i++) {
			Ognl.setValue(arg, index, count);
			if (count++ > 0) {
				append(context, separator);
			}
			sqlNode.execute(context, arg);
		}
		return count;
	}

	private int foreachIntArray(Object target, ServiceContext context, Object arg) throws Throwable {
		int[] array = (int[]) target;
		for (int i = 0; i < array.length; i++) {
			Ognl.setValue(arg, index, i);
			if (i > 0) {
				append(context, separator);
			}
			sqlNode.execute(context, arg);
		}
		return array.length;
	}

	private int foreachLongArray(Object target, ServiceContext context, Object arg) throws Throwable {
		long[] array = (long[]) target;
		for (int i = 0; i < array.length; i++) {
			Ognl.setValue(arg, index, i);
			if (i > 0) {
				append(context, separator);
			}
			sqlNode.execute(context, arg);
		}
		return array.length;
	}

	private int foreachBooleanArray(Object target, ServiceContext context, Object arg) throws Throwable {
		boolean[] array = (boolean[]) target;
		for (int i = 0; i < array.length; i++) {
			Ognl.setValue(arg, index, i);
			if (i > 0) {
				append(context, separator);
			}
			sqlNode.execute(context, arg);
		}
		return array.length;
	}

	private int foreachByteArray(Object target, ServiceContext context, Object arg) throws Throwable {
		byte[] array = (byte[]) target;
		for (int i = 0; i < array.length; i++) {
			Ognl.setValue(arg, index, i);
			if (i > 0) {
				append(context, separator);
			}
			sqlNode.execute(context, arg);
		}
		return array.length;
	}

	private int foreachCharArray(Object target, ServiceContext context, Object arg) throws Throwable {
		char[] array = (char[]) target;
		for (int i = 0; i < array.length; i++) {
			Ognl.setValue(arg, index, i);
			if (i > 0) {
				append(context, separator);
			}
			sqlNode.execute(context, arg);
		}
		return array.length;
	}

	private int foreachDoubleArray(Object target, ServiceContext context, Object arg) throws Throwable {
		double[] array = (double[]) target;
		for (int i = 0; i < array.length; i++) {
			Ognl.setValue(arg, index, i);
			if (i > 0) {
				append(context, separator);
			}
			sqlNode.execute(context, arg);
		}
		return array.length;
	}

	private int foreachFloatArray(Object target, ServiceContext context, Object arg) throws Throwable {
		float[] array = (float[]) target;
		for (int i = 0; i < array.length; i++) {
			Ognl.setValue(arg, index, i);
			if (i > 0) {
				append(context, separator);
			}
			sqlNode.execute(context, arg);
		}
		return array.length;
	}

	private int foreachShortArray(Object target, ServiceContext context, Object arg) throws Throwable {
		short[] array = (short[]) target;
		for (int i = 0; i < array.length; i++) {
			Ognl.setValue(arg, index, i);
			if (i > 0) {
				append(context, separator);
			}
			sqlNode.execute(context, arg);
		}
		return array.length;
	}

}
