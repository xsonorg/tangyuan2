package org.xson.tangyuan.hbase.util;

import java.math.BigDecimal;

import org.apache.hadoop.hbase.util.Bytes;
import org.xson.tangyuan.hbase.executor.hbase.HBaseCommondParsedException;

public class HBaseUtil {

	public static byte[] toByte(Object val) {
		if (val instanceof String) {
			return Bytes.toBytes((String) val);
		}

		if (val instanceof Integer) {
			return Bytes.toBytes((Integer) val);
		}

		if (val instanceof Long) {
			return Bytes.toBytes((Long) val);
		}

		if (val instanceof Float) {
			return Bytes.toBytes((Float) val);
		}

		if (val instanceof Double) {
			return Bytes.toBytes((Double) val);
		}

		if (val instanceof Boolean) {
			return Bytes.toBytes((Boolean) val);
		}

		if (val instanceof Short) {
			return Bytes.toBytes((Short) val);
		}

		if (val instanceof BigDecimal) {
			return Bytes.toBytes((BigDecimal) val);
		}
		return null;
	}

	public static void checkNull(Object val, String errorMessage) {
		if (null == val) {
			throw new HBaseCommondParsedException(errorMessage);
		}
	}
}
