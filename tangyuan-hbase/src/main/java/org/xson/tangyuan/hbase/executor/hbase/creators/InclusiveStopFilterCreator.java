package org.xson.tangyuan.hbase.executor.hbase.creators;

import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.InclusiveStopFilter;
import org.xson.tangyuan.hbase.executor.hbase.FilterCreator;
import org.xson.tangyuan.hbase.util.HBaseUtil;

import com.alibaba.fastjson.JSONObject;

public class InclusiveStopFilterCreator extends FilterCreator {

	@Override
	public Filter create(JSONObject jsonFilter) {
		Object stopRowKey = jsonFilter.get("stopRowKey");
		checkNull(stopRowKey, "the InclusiveStopFilter is missing the 'stopRowKey' property.");
		InclusiveStopFilter filter = new InclusiveStopFilter(HBaseUtil.toByte(stopRowKey));
		return filter;
	}

}
