package org.xson.tangyuan.hbase.executor.hbase.creators;

import org.apache.hadoop.hbase.filter.ByteArrayComparable;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.xson.tangyuan.hbase.executor.hbase.FilterCreator;
import org.xson.tangyuan.hbase.util.HBaseUtil;

import com.alibaba.fastjson.JSONObject;

public class SingleColumnValueFilterCreator extends FilterCreator {

	@Override
	public Filter create(JSONObject jsonFilter) {

		String family = jsonFilter.getString("family");
		String qualifier = jsonFilter.getString("qualifier");

		String compareOp = jsonFilter.getString("compareOp");
		Object value = jsonFilter.get("value");
		String comparableName = jsonFilter.getString("comparator");

		checkNull(family, "the SingleColumnValueFilter is missing the 'family' property.");
		checkNull(qualifier, "the SingleColumnValueFilter is missing the 'qualifier' property.");
		checkNull(compareOp, "the SingleColumnValueFilter is missing the 'compareOp' property.");
		checkNull(value, "the SingleColumnValueFilter is missing the 'value' property.");

		// checkNull(comparableName, "the SingleColumnValueFilter is missing the 'comparator' property.");

		CompareOp op = parseCompareOp(compareOp);
		ByteArrayComparable bc = null;
		if (null != comparableName) {
			bc = parseComparable(comparableName, value);
		}

		SingleColumnValueFilter filter = null;
		if (null != comparableName) {
			filter = new SingleColumnValueFilter(HBaseUtil.toByte(family), HBaseUtil.toByte(qualifier), op, HBaseUtil.toByte(value));
		} else {
			filter = new SingleColumnValueFilter(HBaseUtil.toByte(family), HBaseUtil.toByte(qualifier), op, bc);

		}
		return filter;
	}

}