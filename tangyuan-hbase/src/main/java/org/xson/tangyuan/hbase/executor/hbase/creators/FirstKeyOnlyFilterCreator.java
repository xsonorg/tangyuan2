package org.xson.tangyuan.hbase.executor.hbase.creators;

import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.xson.tangyuan.hbase.executor.hbase.FilterCreator;

import com.alibaba.fastjson.JSONObject;

public class FirstKeyOnlyFilterCreator extends FilterCreator {

	@Override
	public Filter create(JSONObject jsonFilter) {
		FirstKeyOnlyFilter filter = new FirstKeyOnlyFilter();
		return filter;
	}

}
