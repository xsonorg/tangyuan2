package org.xson.tangyuan.hbase.executor.hbase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.xson.tangyuan.hbase.util.HBaseUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class PutVo extends BaseVo {

	class FamilyVo {

		private String				name;

		private Map<String, Object>	columns;

		public FamilyVo(String name, Map<String, Object> columns) {
			this.name = name;
			this.columns = columns;
		}

		public String getName() {
			return name;
		}

		public Map<String, Object> getColumns() {
			return columns;
		}
	}

	private Durability		durability;
	private List<FamilyVo>	families;

	@SuppressWarnings("unchecked")
	public void parse(String text) {
		JSONObject json = JSON.parseObject(text);

		String ns = json.getString("ns");// N
		String table = json.getString("table");// Y
		String row = json.getString("row");// Y

		String _durability = json.getString("durability");// Y

		List<FamilyVo> families = null;

		JSONObject family = json.getJSONObject("family");// N
		if (null != family && family.size() > 0) {
			families = new ArrayList<FamilyVo>();
			for (Entry<String, Object> item : family.entrySet()) {
				String key = item.getKey();
				Object value = item.getValue();
				// check
				if (!(value instanceof JSONObject)) {
					throw new HBaseCommondParsedException("'family' property structure error.\n" + text);
				}
				Map<String, Object> columns = (Map<String, Object>) value;
				families.add(new FamilyVo(key, columns));
			}
		}

		// check
		HBaseUtil.checkNull(table, "'table' property can not be empty.\n" + text);
		HBaseUtil.checkNull(row, "'row' property can not be empty.\n" + text);

		this.ns = ns;
		this.table = table;
		this.row = row;

		this.durability = parseDurability(_durability);
		this.families = families;
	}

	public String getTableName() {
		if (null == this.ns || 0 == this.ns.length()) {
			return this.table;
		}
		return this.ns + ":" + table;
	}

	public Put getHBasePut() throws Throwable {
		Put hbasePut = new Put(Bytes.toBytes(this.row));
		for (FamilyVo familyVo : this.families) {
			byte[] familyNameBytes = Bytes.toBytes(familyVo.getName());
			Map<String, Object> columns = familyVo.getColumns();
			for (Entry<String, Object> item : columns.entrySet()) {
				hbasePut.addColumn(familyNameBytes, Bytes.toBytes(item.getKey()), Bytes.toBytes(item.getValue().toString()));
			}
		}

		// set
		if (null != this.durability) {
			hbasePut.setDurability(this.durability);
		}
		// hbasePut.setWriteToWAL(write)

		return hbasePut;
	}
}
