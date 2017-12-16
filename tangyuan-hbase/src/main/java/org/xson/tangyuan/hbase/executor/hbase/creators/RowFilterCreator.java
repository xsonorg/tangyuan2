package org.xson.tangyuan.hbase.executor.hbase.creators;

import org.apache.hadoop.hbase.filter.ByteArrayComparable;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.xson.tangyuan.hbase.executor.hbase.FilterCreator;

import com.alibaba.fastjson.JSONObject;

public class RowFilterCreator extends FilterCreator {

	@Override
	public Filter create(JSONObject jsonFilter) {
		String compareOp = jsonFilter.getString("compareOp");
		Object value = jsonFilter.get("value");
		String comparableName = jsonFilter.getString("comparator");

		checkNull(compareOp, "the RowFilter is missing the 'compareOp' property.");
		checkNull(value, "the RowFilter is missing the 'value' property.");
		checkNull(comparableName, "the RowFilter is missing the 'comparator' property.");

		CompareOp op = parseCompareOp(compareOp);
		ByteArrayComparable bc = parseComparable(comparableName, value);

		RowFilter filter = new RowFilter(op, bc);
		return filter;
	}

}
