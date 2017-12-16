package org.xson.tangyuan.hbase.executor.hbase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.util.Bytes;
import org.xson.tangyuan.hbase.util.HBaseUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class GetVo extends BaseVo{

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

	private Long			timestamp;
	private long[]			timeRange;
	private Boolean			cacheBlocks;
	private Integer			maxVersions;

	private List<FamilyVo>	families;

	@SuppressWarnings("unchecked")
	public void parse(String text) {
		JSONObject json = JSON.parseObject(text);

		String ns = json.getString("ns");// N
		String table = json.getString("table");// Y
		String row = json.getString("row");// Y

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
				// check
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

		// check
		HBaseUtil.checkNull(table, "'table' property can not be empty.\n" + text);
		HBaseUtil.checkNull(row, "'row' property can not be empty.\n" + text);

		this.ns = ns;
		this.table = table;
		this.row = row;
		if (null != timeRangeArray && 2 == timeRangeArray.size()) {
			this.timeRange = new long[2];
			this.timeRange[0] = (Long) timeRangeArray.get(0);
			this.timeRange[1] = (Long) timeRangeArray.get(1);
		}
		this.timestamp = timestamp;
		this.maxVersions = maxVersions;
		this.cacheBlocks = cacheBlocks;
		this.families = families;
	}

	public String getTableName() {
		if (null == this.ns || 0 == this.ns.length()) {
			return this.table;
		}
		return this.ns + ":" + table;
	}

	public Get getHBaseGet() throws Throwable {

		Get hbaseGet = new Get(Bytes.toBytes(this.row));
		if (null != this.families) {
			for (FamilyVo familyVo : this.families) {
				if (null == familyVo.getColumns()) {
					hbaseGet.addFamily(Bytes.toBytes(familyVo.getName()));
				} else {
					byte[] familyNameBytes = null;
					for (String column : familyVo.getColumns()) {
						if (null == familyNameBytes) {
							familyNameBytes = Bytes.toBytes(familyVo.getName());
						}
						hbaseGet.addColumn(familyNameBytes, Bytes.toBytes(column));
					}
				}
			}
		}

		if (null != this.cacheBlocks) {
			hbaseGet.setCacheBlocks(this.cacheBlocks);
		}

		if (null != this.timeRange) {
			hbaseGet.setTimeRange(this.timeRange[0], this.timeRange[1]);
		}

		if (null != this.timestamp) {
			hbaseGet.setTimeStamp(timestamp);
		}

		if (null != this.maxVersions) {
			int maxVal = this.maxVersions.intValue();
			if (maxVal < 0) {
				hbaseGet.setMaxVersions();// 获取所有版本的数据
			} else {
				hbaseGet.setMaxVersions(maxVal);
			}
		}

		return hbaseGet;
	}

}
