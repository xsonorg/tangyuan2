package org.xson.tangyuan.hbase.executor.hbase.creators;

import org.apache.hadoop.hbase.filter.ByteArrayComparable;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.SingleColumnValueExcludeFilter;
import org.xson.tangyuan.hbase.executor.hbase.FilterCreator;
import org.xson.tangyuan.hbase.util.HBaseUtil;

import com.alibaba.fastjson.JSONObject;

public class SingleColumnValueExcludeFilterCreator extends FilterCreator {

	@Override
	public Filter create(JSONObject jsonFilter) {

		String family = jsonFilter.getString("family");
		String qualifier = jsonFilter.getString("qualifier");

		String compareOp = jsonFilter.getString("compareOp");
		Object value = jsonFilter.get("value");
		String comparableName = jsonFilter.getString("comparator");

		checkNull(family, "the SingleColumnValueExcludeFilter is missing the 'family' property.");
		checkNull(qualifier, "the SingleColumnValueExcludeFilter is missing the 'qualifier' property.");
		checkNull(compareOp, "the SingleColumnValueExcludeFilter is missing the 'compareOp' property.");
		checkNull(value, "the SingleColumnValueExcludeFilter is missing the 'value' property.");

		// checkNull(comparableName, "the SingleColumnValueFilter is missing the 'comparator' property.");

		CompareOp op = parseCompareOp(compareOp);
		ByteArrayComparable bc = null;
		if (null != comparableName) {
			bc = parseComparable(comparableName, value);
		}

		SingleColumnValueExcludeFilter filter = null;
		if (null != comparableName) {
			filter = new SingleColumnValueExcludeFilter(HBaseUtil.toByte(family), HBaseUtil.toByte(qualifier), op, HBaseUtil.toByte(value));
		} else {
			filter = new SingleColumnValueExcludeFilter(HBaseUtil.toByte(family), HBaseUtil.toByte(qualifier), op, bc);

		}
		return filter;
	}

}