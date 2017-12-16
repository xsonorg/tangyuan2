package org.xson.tangyuan.hbase.executor.hbase.creators;

import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.MultipleColumnPrefixFilter;
import org.xson.tangyuan.hbase.executor.hbase.FilterCreator;
import org.xson.tangyuan.hbase.util.HBaseUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class MultipleColumnPrefixFilterCreator extends FilterCreator {

	@Override
	public Filter create(JSONObject jsonFilter) {
		Object _prefixes = jsonFilter.get("prefixes");
		checkNull(_prefixes, "the MultipleColumnPrefixFilter is missing the 'prefixes' property.");

		JSONArray array = (JSONArray) _prefixes;
		checkLength(array, "the element in 'prefixes' can not be empty.");

		byte[][] prefixes = new byte[array.size()][];
		for (int i = 0; i < prefixes.length; i++) {
			prefixes[i] = HBaseUtil.toByte(array.get(i));
		}

		MultipleColumnPrefixFilter filter = new MultipleColumnPrefixFilter(prefixes);
		return filter;
	}

}
