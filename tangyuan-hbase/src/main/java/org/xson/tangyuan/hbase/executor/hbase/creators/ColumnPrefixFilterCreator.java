package org.xson.tangyuan.hbase.executor.hbase.creators;

import org.apache.hadoop.hbase.filter.ColumnPrefixFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.xson.tangyuan.hbase.executor.hbase.FilterCreator;
import org.xson.tangyuan.hbase.util.HBaseUtil;

import com.alibaba.fastjson.JSONObject;

public class ColumnPrefixFilterCreator extends FilterCreator {

	@Override
	public Filter create(JSONObject jsonFilter) {
		Object prefix = jsonFilter.get("prefix");
		checkNull(prefix, "the ColumnPrefixFilter is missing the 'prefix' property.");
		ColumnPrefixFilter filter = new ColumnPrefixFilter(HBaseUtil.toByte(prefix));
		return filter;
	}

}
