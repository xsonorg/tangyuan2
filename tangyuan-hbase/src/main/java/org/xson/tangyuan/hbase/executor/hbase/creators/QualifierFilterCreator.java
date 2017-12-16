package org.xson.tangyuan.hbase.executor.hbase.creators;

import org.apache.hadoop.hbase.filter.ByteArrayComparable;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.QualifierFilter;
import org.xson.tangyuan.hbase.executor.hbase.FilterCreator;

import com.alibaba.fastjson.JSONObject;

public class QualifierFilterCreator extends FilterCreator {

	@Override
	public Filter create(JSONObject jsonFilter) {
		String compareOp = jsonFilter.getString("compareOp");
		Object value = jsonFilter.get("value");
		String comparableName = jsonFilter.getString("comparator");

		checkNull(compareOp, "the QualifierFilter is missing the 'compareOp' property.");
		checkNull(value, "the QualifierFilter is missing the 'value' property.");
		checkNull(comparableName, "the QualifierFilter is missing the 'comparator' property.");

		CompareOp op = parseCompareOp(compareOp);
		ByteArrayComparable bc = parseComparable(comparableName, value);

		QualifierFilter filter = new QualifierFilter(op, bc);
		return filter;
	}

}
