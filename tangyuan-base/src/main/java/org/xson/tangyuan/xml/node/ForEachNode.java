package org.xson.tangyuan.xml.node;

import java.util.Collection;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.ognl.Ognl;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.service.ActuatorContext;

public abstract class ForEachNode implements TangYuanNode {

	protected TangYuanNode	sqlNode;
	/** 集合变量(从那个集合中遍历, 不能为空) */
	protected Variable		collection;
	/** 集合中索引的变量名称 */
	protected String		index;

	protected String		open;
	protected String		close;
	protected String		separator;

	@Override
	public boolean execute(ActuatorContext ac, Object arg, Object temp) throws Throwable {
		int count = 0;
		Object obj = collection.getValue(temp);
		if (null == obj) {
			throw new TangYuanException("in the <foreach> node, the collection[" + collection.getOriginal() + "] is empty.");
		}
		append(ac, open);

		if (null != obj && obj instanceof Collection) {
			count = foreachCollection(obj, ac, arg, temp);
		} else if (null != obj && obj.getClass().isArray()) {
			Class<?> clazz = obj.getClass();
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
			count = foreachArray(ac, arg, temp, arrayLength);
		} else {
			// 必须是集合类型
			throw new TangYuanException("Unsupported collection[" + collection.getOriginal() + "] type: " + obj.getClass().getName());
		}

		if (0 == count) {
			// 集合数量>0
			throw new TangYuanException("the number of elements that can be processed in the collection[" + collection.getOriginal() + "] is 0.");
		}

		append(ac, close);
		return true;
	}

	protected abstract void append(ActuatorContext ac, String str);

	@SuppressWarnings("unused")
	private int foreachCollection(Object target, ActuatorContext ac, Object arg, Object temp) throws Throwable {
		Collection<?> collection = (Collection<?>) target;
		int count = 0;
		for (Object item : collection) {
			// if (null == item) { 对于用户的操作未知, 不能抛出异常
			Ognl.setValue(temp, index, count);
			if (count++ > 0) {
				append(ac, separator);
			}
			sqlNode.execute(ac, arg, temp);
		}
		return count;
	}

	private int foreachArray(ActuatorContext ac, Object arg, Object temp, int arrayLength) throws Throwable {
		int i = 0;
		int iEnd = arrayLength;
		for (; i < iEnd; i++) {
			Ognl.setValue(temp, index, i);
			if (i > 0) {
				append(ac, separator);
			}
			sqlNode.execute(ac, arg, temp);
		}
		return i;
	}

}
