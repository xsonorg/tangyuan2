package org.xson.tangyuan.hbase.executor.hbase.creators;

import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.SkipFilter;
import org.apache.hadoop.hbase.filter.ValueFilter;
import org.xson.tangyuan.hbase.executor.hbase.FilterCreator;

import com.alibaba.fastjson.JSONObject;

public class SkipFilterCreator extends FilterCreator {

	@Override
	public Filter create(JSONObject jsonFilter) {
		Filter valueFilter = new ValueFilterCreator().create(jsonFilter);
		SkipFilter filter = new SkipFilter((ValueFilter) valueFilter);
		return filter;
	}

}