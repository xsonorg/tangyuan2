package org.xson.tangyuan.hbase.executor.hbase;

import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.BinaryPrefixComparator;
import org.apache.hadoop.hbase.filter.ByteArrayComparable;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.SubstringComparator;
import org.xson.tangyuan.hbase.util.HBaseUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public abstract class FilterCreator {

	abstract public Filter create(JSONObject jsonFilter);

	protected void checkNull(Object val, String errorMessage) {
		if (null == val) {
			throw new HBaseCommondParsedException(errorMessage);
		}
	}

	protected void checkLength(JSONArray val, String errorMessage) {
		if (0 == val.size()) {
			throw new HBaseCommondParsedException(errorMessage);
		}
	}

	protected CompareOp parseCompareOp(String val) {
		if ("LESS".equalsIgnoreCase(val) || "<".equals(val)) {
			return CompareOp.LESS;
		} else if ("LESS_OR_EQUAL".equalsIgnoreCase(val) || "<=".equals(val)) {
			return CompareOp.LESS_OR_EQUAL;
		} else if ("EQUAL".equalsIgnoreCase(val) || "=".equals(val) || "==".equals(val)) {
			return CompareOp.EQUAL;
		} else if ("NOT_EQUAL".equalsIgnoreCase(val) || "!=".equals(val) || "<>".equals(val)) {
			return CompareOp.NOT_EQUAL;
		} else if ("GREATER_OR_EQUAL".equalsIgnoreCase(val) || ">=".equals(val)) {
			return CompareOp.GREATER_OR_EQUAL;
		} else if ("GREATER".equalsIgnoreCase(val) || ">".equals(val)) {
			return CompareOp.GREATER;
		} else if ("NO_OP".equalsIgnoreCase(val)) {
			return CompareOp.NO_OP;
		}
		throw new HBaseCommondParsedException("unsupported comparison symbol: " + val);
	}

	protected ByteArrayComparable parseComparable(String comparableName, Object value) {

		if ("BinaryComparator".equalsIgnoreCase(comparableName)) {
			return new BinaryComparator(HBaseUtil.toByte(value));
		} else if ("BinaryPrefixComparator".equalsIgnoreCase(comparableName)) {
			return new BinaryPrefixComparator(HBaseUtil.toByte(value));
		}

		// else if("BitComparator".equalsIgnoreCase(comparableName)){
		// return new BitComparator(value, bitOperator);
		// }else if("LongComparator".equalsIgnoreCase(comparableName)){
		// return new LongComparator(value);
		// }else if("NullComparator".equalsIgnoreCase(comparableName)){
		// return new NullComparator(nullsAreHigh)
		// }

		else if ("RegexStringComparator".equalsIgnoreCase(comparableName)) {
			return new RegexStringComparator((String) value);
		} else if ("SubstringComparator".equalsIgnoreCase(comparableName)) {
			return new SubstringComparator((String) value);
		}
		throw new HBaseCommondParsedException("unsupported comparator: " + comparableName);
	}
}
