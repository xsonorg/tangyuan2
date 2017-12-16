package org.xson.tangyuan.hbase.executor.hbase.creators;

import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.xson.tangyuan.hbase.executor.hbase.FilterCreator;
import org.xson.tangyuan.hbase.util.HBaseUtil;

import com.alibaba.fastjson.JSONObject;

public class PrefixFilterCreator extends FilterCreator {

	@Override
	public Filter create(JSONObject jsonFilter) {
		Object prefix = jsonFilter.get("prefix");
		checkNull(prefix, "the PrefixFilter is missing the 'prefix' property.");
		PrefixFilter filter = new PrefixFilter(HBaseUtil.toByte(prefix));
		return filter;
	}

}
