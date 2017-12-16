package org.xson.tangyuan.hbase.executor.hbase.creators;

import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.xson.tangyuan.hbase.executor.hbase.FilterCreator;

import com.alibaba.fastjson.JSONObject;

public class PageFilterCreator extends FilterCreator {

	@Override
	public Filter create(JSONObject jsonFilter) {
		Long pageSize = jsonFilter.getLong("pageSize");
		checkNull(pageSize, "the PageFilter is missing the 'pageSize' property.");
		PageFilter filter = new PageFilter(pageSize);
		return filter;
	}

}
