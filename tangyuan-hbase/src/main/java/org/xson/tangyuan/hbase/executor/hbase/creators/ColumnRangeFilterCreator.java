package org.xson.tangyuan.hbase.executor.hbase.creators;

import org.apache.hadoop.hbase.filter.ColumnRangeFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.xson.tangyuan.hbase.executor.hbase.FilterCreator;
import org.xson.tangyuan.hbase.util.HBaseUtil;

import com.alibaba.fastjson.JSONObject;

public class ColumnRangeFilterCreator extends FilterCreator {

	@Override
	public Filter create(JSONObject jsonFilter) {
		Object minColumn = jsonFilter.get("minColumn");
		Object maxColumn = jsonFilter.get("maxColumn");

		byte[] minColumnVal = HBaseUtil.toByte(minColumn);
		byte[] maxColumnVal = HBaseUtil.toByte(maxColumn);

		// checkNull(minColumn, "the ColumnPrefixFilter is missing the 'minColumn' property.");
		// checkNull(maxColumn, "the ColumnPrefixFilter is missing the 'maxColumn' property.");

		ColumnRangeFilter filter = new ColumnRangeFilter(minColumnVal, true, maxColumnVal, true);
		return filter;
	}

}