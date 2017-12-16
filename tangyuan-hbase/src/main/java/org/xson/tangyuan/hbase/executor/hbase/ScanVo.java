package org.xson.tangyuan.hbase.executor.hbase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.filter.FilterList.Operator;
import org.apache.hadoop.hbase.util.Bytes;
import org.xson.tangyuan.hbase.util.HBaseUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class ScanVo extends BaseVo {

	class FamilyVo {

		private String			name;

		private List<String>	columns;

		public FamilyVo(String name, List<String> columns) {
			this.name = name;
			this.columns = columns;
		}

		public String getName() {
			return name;
		}

		public List<String> getColumns() {
			return columns;
		}
	}

	private int				pageSize		= 20;
	private int				tempPageSize	= pageSize;
	private byte[]			startRowKey		= null;
	private byte[]			stopRowKey		= null;

	private Boolean			includeStartRow	= null;

	private Boolean			reversed		= null;
	private long[]			timeRange		= null;
	private Boolean			cacheBlocks		= null;
	private Integer			caching			= null;
	private Long			timestamp		= null;

	private Integer			maxVersions		= null;

	private List<FamilyVo>	families		= null;

	private Filter			scanFilter		= null;

	@SuppressWarnings("unchecked")
	public void parse(String text) {

		JSONObject json = JSON.parseObject(text);

		String ns = json.getString("ns");// N
		String table = json.getString("table");// Y

		Integer pageSize = json.getInteger("pageSize");

		Object startRowKey = json.get("startRowKey");
		Object stopRowKey = json.get("stopRowKey");

		Boolean includeStartRow = json.getBoolean("includeStartRow");

		Boolean reversed = json.getBoolean("reversed");
		Integer caching = json.getInteger("caching");
		Boolean cacheBlocks = json.getBoolean("cacheBlocks");
		Long timestamp = json.getLong("timestamp");
		Integer maxVersions = json.getInteger("maxVersions");
		List<Object> timeRangeArray = json.getJSONArray("timeRange");

		List<FamilyVo> families = null;

		JSONObject family = json.getJSONObject("family");// N
		if (null != family && family.size() > 0) {
			families = new ArrayList<FamilyVo>();
			for (Entry<String, Object> item : family.entrySet()) {
				String key = item.getKey();
				Object value = item.getValue();
				if (!(value instanceof List)) {
					throw new HBaseCommondParsedException("'family' property structure error.\n" + text);
				}
				List<String> columns = (List<String>) value;
				if (0 == columns.size()) {
					families.add(new FamilyVo(key, null));
				} else {
					families.add(new FamilyVo(key, columns));
				}
			}
		}

		JSONObject filter = json.getJSONObject("filter");// N
		parseFilter(filter);
		initFilter();

		// check
		HBaseUtil.checkNull(table, "'table' property can not be empty.\n" + text);

		this.ns = ns;
		this.table = table;

		if (null != pageSize) {
			this.pageSize = pageSize;
		}
		if (null != startRowKey) {
			this.startRowKey = HBaseUtil.toByte(startRowKey);
			if (null == this.startRowKey) {
				throw new HBaseCommondParsedException("'startRowKey' data type is not supported.");
			}
		}
		if (null != stopRowKey) {
			this.stopRowKey = HBaseUtil.toByte(stopRowKey);
		}
		if (null != timeRangeArray && 2 == timeRangeArray.size()) {
			this.timeRange = new long[2];
			this.timeRange[0] = (Long) timeRangeArray.get(0);
			this.timeRange[1] = (Long) timeRangeArray.get(1);
		}
		this.reversed = reversed;
		this.cacheBlocks = cacheBlocks;
		this.caching = caching;
		this.timestamp = timestamp;
		this.includeStartRow = includeStartRow;
		this.maxVersions = maxVersions;
		this.families = families;
	}

	private void initFilter() {
		if (null == tempFilterList || 0 == tempFilterList.size()) {
			this.scanFilter = new PageFilter(this.pageSize + 1);
			return;
		}

		this.tempFilterList.add(new PageFilter(this.pageSize + 1));
		Operator operator = this.filterListOperator;
		if (null == operator) {
			operator = Operator.MUST_PASS_ALL;
		}

		this.scanFilter = new FilterList(operator, this.tempFilterList);
	}

	public String getTableName() {
		if (null == this.ns || 0 == this.ns.length()) {
			return this.table;
		}
		return this.ns + ":" + table;
	}

	public int getTempPageSize() {
		return this.tempPageSize;
	}

	public byte[] getStartRowKey() {
		return startRowKey;
	}

	public Boolean getIncludeStartRow() {
		if (null != this.includeStartRow) {
			return this.includeStartRow;
		}
		return null == this.startRowKey;
	}

	public Filter getFilter() {
		return scanFilter;
	}

	public Scan getHBaseScan(byte[] firstRowKey) throws Throwable {
		if (null == firstRowKey) {
			this.tempPageSize++;
		} else {
			this.startRowKey = firstRowKey;
		}

		Scan scan = new Scan();

		scan.setStartRow(this.startRowKey);
		if (null != this.stopRowKey) {
			scan.setStopRow(this.stopRowKey);
		}

		if (null != this.timeRange) {
			scan.setTimeRange(this.timeRange[0], this.timeRange[1]);
		}

		if (null != this.timestamp) {
			scan.setTimeStamp(timestamp);
		}

		if (null != this.reversed) {
			scan.setReversed(reversed);
		}

		if (null != this.cacheBlocks) {
			scan.setCacheBlocks(this.cacheBlocks);
		}

		if (null != this.caching) {
			scan.setCaching(caching);
		}

		if (null != this.maxVersions) {
			int maxVal = this.maxVersions.intValue();
			if (maxVal < 0) {
				scan.setMaxVersions();// 获取所有版本的数据
			} else {
				scan.setMaxVersions(maxVal);
			}
		}

		if (null != this.families) {
			for (FamilyVo familyVo : this.families) {
				if (null == familyVo.getColumns()) {
					scan.addFamily(Bytes.toBytes(familyVo.getName()));
				} else {
					byte[] familyNameBytes = null;
					for (String column : familyVo.getColumns()) {
						if (null == familyNameBytes) {
							familyNameBytes = Bytes.toBytes(familyVo.getName());
						}
						scan.addColumn(familyNameBytes, Bytes.toBytes(column));
					}
				}
			}
		}

		if (null != this.scanFilter) {
			scan.setFilter(this.scanFilter);
		}

		// Scan setStartRow (byte[] startRow) 设置Scan的开始行， 默认 结果集 包含 该行。 如果希望结果集不包含该行，可以在行键末尾加上0。
		// Scan setStopRow (byte[] stopRow) 设置Scan的结束行， 默认 结果集 不包含该行。 如果希望结果集包含该行，可以在行键末尾加上0。

		return scan;
	}
}
