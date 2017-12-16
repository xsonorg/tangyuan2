package org.xson.tangyuan.hbase.executor.hbase.creators;

import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.KeyOnlyFilter;
import org.xson.tangyuan.hbase.executor.hbase.FilterCreator;

import com.alibaba.fastjson.JSONObject;

public class KeyOnlyFilterCreator extends FilterCreator {

	@Override
	public Filter create(JSONObject jsonFilter) {
		KeyOnlyFilter filter = new KeyOnlyFilter();
		return filter;
	}

}
