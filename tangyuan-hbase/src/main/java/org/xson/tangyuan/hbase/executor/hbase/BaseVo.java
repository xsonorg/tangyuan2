package org.xson.tangyuan.hbase.executor.hbase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList.Operator;
import org.xson.tangyuan.hbase.executor.hbase.creators.ColumnPrefixFilterCreator;
import org.xson.tangyuan.hbase.executor.hbase.creators.ColumnRangeFilterCreator;
import org.xson.tangyuan.hbase.executor.hbase.creators.FirstKeyOnlyFilterCreator;
import org.xson.tangyuan.hbase.executor.hbase.creators.InclusiveStopFilterCreator;
import org.xson.tangyuan.hbase.executor.hbase.creators.KeyOnlyFilterCreator;
import org.xson.tangyuan.hbase.executor.hbase.creators.MultipleColumnPrefixFilterCreator;
import org.xson.tangyuan.hbase.executor.hbase.creators.PageFilterCreator;
import org.xson.tangyuan.hbase.executor.hbase.creators.PrefixFilterCreator;
import org.xson.tangyuan.hbase.executor.hbase.creators.QualifierFilterCreator;
import org.xson.tangyuan.hbase.executor.hbase.creators.RowFilterCreator;
import org.xson.tangyuan.hbase.executor.hbase.creators.SingleColumnValueExcludeFilterCreator;
import org.xson.tangyuan.hbase.executor.hbase.creators.SingleColumnValueFilterCreator;
import org.xson.tangyuan.hbase.executor.hbase.creators.SkipFilterCreator;
import org.xson.tangyuan.hbase.executor.hbase.creators.TimestampsFilterCreator;
import org.xson.tangyuan.hbase.executor.hbase.creators.ValueFilterCreator;

import com.alibaba.fastjson.JSONObject;

public class BaseVo {

	private static Map<String, FilterCreator> filterCreatorMap = new HashMap<String, FilterCreator>();

	static {
		filterCreatorMap.put("ColumnPrefixFilter".toUpperCase(), new ColumnPrefixFilterCreator());
		filterCreatorMap.put("ColumnRangeFilter".toUpperCase(), new ColumnRangeFilterCreator());
		filterCreatorMap.put("QualifierFilter".toUpperCase(), new QualifierFilterCreator());
		filterCreatorMap.put("RowFilter".toUpperCase(), new RowFilterCreator());
		filterCreatorMap.put("ValueFilter".toUpperCase(), new ValueFilterCreator());
		filterCreatorMap.put("FirstKeyOnlyFilter".toUpperCase(), new FirstKeyOnlyFilterCreator());
		filterCreatorMap.put("InclusiveStopFilter".toUpperCase(), new InclusiveStopFilterCreator());
		filterCreatorMap.put("KeyOnlyFilter".toUpperCase(), new KeyOnlyFilterCreator());
		filterCreatorMap.put("MultipleColumnPrefixFilter".toUpperCase(), new MultipleColumnPrefixFilterCreator());
		filterCreatorMap.put("PageFilter".toUpperCase(), new PageFilterCreator());
		filterCreatorMap.put("PrefixFilter".toUpperCase(), new PrefixFilterCreator());
		filterCreatorMap.put("SingleColumnValueFilter".toUpperCase(), new SingleColumnValueFilterCreator());
		filterCreatorMap.put("SingleColumnValueExcludeFilter".toUpperCase(), new SingleColumnValueExcludeFilterCreator());
		filterCreatorMap.put("SkipFilter".toUpperCase(), new SkipFilterCreator());
		filterCreatorMap.put("TimestampsFilter".toUpperCase(), new TimestampsFilterCreator());
	}

	protected String		ns					= null;
	protected String		table				= null;
	// protected String row = null;

	protected List<Filter>	tempFilterList		= null;
	protected Operator		filterListOperator	= null;

	protected void parseFilter(JSONObject jsonFilter) {
		if (null == jsonFilter || 0 == jsonFilter.size()) {
			return;
		}
		this.tempFilterList = new ArrayList<Filter>();
		for (Entry<String, Object> entry : jsonFilter.entrySet()) {
			String key = entry.getKey();
			FilterCreator creator = filterCreatorMap.get(key.toUpperCase());
			if (null != creator) {
				this.tempFilterList.add(creator.create((JSONObject) entry.getValue()));
				continue;
			}
			if ("operator".equalsIgnoreCase(key)) {
				this.filterListOperator = parseOperator((String) entry.getValue());
				continue;
			}
			throw new HBaseCommondParsedException("unsupported filter: " + key);
		}
	}

	private Operator parseOperator(String val) {
		if ("MUST_PASS_ALL".equalsIgnoreCase(val)) {
			return Operator.MUST_PASS_ALL;
		} else if ("MUST_PASS_ONE".equalsIgnoreCase(val)) {
			return Operator.MUST_PASS_ONE;
		}
		throw new HBaseCommondParsedException("unsupported operator: " + val);
	}

	protected Durability parseDurability(String durability) {
		if ("USE_DEFAULT".equalsIgnoreCase(durability)) {
			return Durability.USE_DEFAULT;
		} else if ("SKIP_WAL".equalsIgnoreCase(durability)) {
			return Durability.SKIP_WAL;
		} else if ("ASYNC_WAL".equalsIgnoreCase(durability)) {
			return Durability.ASYNC_WAL;
		} else if ("SYNC_WAL".equalsIgnoreCase(durability)) {
			return Durability.SYNC_WAL;
		} else if ("FSYNC_WAL".equalsIgnoreCase(durability)) {
			return Durability.FSYNC_WAL;
		}
		throw new HBaseCommondParsedException("unsupported durability: " + durability);
	}

	public String getTableName() {
		if (null == this.ns || 0 == this.ns.length()) {
			return this.table;
		}
		return this.ns + ":" + table;
	}
}
