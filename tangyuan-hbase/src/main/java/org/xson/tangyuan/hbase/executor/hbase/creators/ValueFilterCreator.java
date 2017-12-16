package org.xson.tangyuan.hbase.executor.hbase.creators;

import org.apache.hadoop.hbase.filter.ByteArrayComparable;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.ValueFilter;
import org.xson.tangyuan.hbase.executor.hbase.FilterCreator;

import com.alibaba.fastjson.JSONObject;

public class ValueFilterCreator extends FilterCreator {

	@Override
	public Filter create(JSONObject jsonFilter) {
		String compareOp = jsonFilter.getString("compareOp");
		Object value = jsonFilter.get("value");
		String comparableName = jsonFilter.getString("comparator");

		checkNull(compareOp, "the ValueFilter is missing the 'compareOp' property.");
		checkNull(value, "the ValueFilter is missing the 'value' property.");
		checkNull(comparableName, "the ValueFilter is missing the 'comparator' property.");

		CompareOp op = parseCompareOp(compareOp);
		ByteArrayComparable bc = parseComparable(comparableName, value);

		ValueFilter filter = new ValueFilter(op, bc);
		return filter;
	}

}
