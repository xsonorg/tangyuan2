package org.xson.tangyuan.hbase.executor.hbase.creators;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.TimestampsFilter;
import org.xson.tangyuan.hbase.executor.hbase.FilterCreator;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class TimestampsFilterCreator extends FilterCreator {

	@Override
	public Filter create(JSONObject jsonFilter) {
		JSONArray timestampArray = jsonFilter.getJSONArray("timestamps");

		checkNull(timestampArray, "the TimestampsFilter is missing the 'timestamps' property.");
		checkLength(timestampArray, "the element in 'timestamps' can not be empty.");

		List<Long> timestampList = new ArrayList<Long>();
		for (Object item : timestampArray) {
			if (item instanceof String) {
				timestampList.add(Long.parseLong((String) item));
			} else if (item instanceof Long) {
				timestampList.add((Long) item);
			} else {
				timestampList.add(Long.parseLong(item.toString()));
			}
		}

		TimestampsFilter filter = new TimestampsFilter(timestampList);
		return filter;
	}

}