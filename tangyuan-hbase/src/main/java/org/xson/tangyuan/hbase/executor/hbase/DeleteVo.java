package org.xson.tangyuan.hbase.executor.hbase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.util.Bytes;
import org.xson.tangyuan.hbase.util.HBaseUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class DeleteVo extends BaseVo {

	class FamilyVo {

		private String			name;
		private Long			timestamp;
		private Long			version;
		private List<ColumnVo>	columns;

		public FamilyVo(String name) {
			this.name = name;
		}

		public FamilyVo(String name, Long timestamp, Long version) {
			this.name = name;
		}

		public FamilyVo(String name, List<ColumnVo> columns) {
			this.name = name;
			this.columns = columns;
		}

		public String getName() {
			return name;
		}

		public List<ColumnVo> getColumns() {
			return columns;
		}

		public Long getTimestamp() {
			return timestamp;
		}

		public Long getVersion() {
			return version;
		}
	}

	class ColumnVo {
		private String		name;
		private Long		timestamp;
		private DeleteScope	scope;

		public ColumnVo(String name, Long timestamp, DeleteScope scope) {
			this.name = name;
			this.timestamp = timestamp;
			this.scope = scope;
		}

		public String getName() {
			return name;
		}

		public Long getTimestamp() {
			return timestamp;
		}

		public DeleteScope getScope() {
			return scope;
		}
	}

	enum DeleteScope {
		ALL, LAST
	}

	private Durability		durability;
	private List<FamilyVo>	families;

	// @SuppressWarnings("unchecked")
	public void parse(String text) {
		JSONObject json = JSON.parseObject(text);

		String ns = json.getString("ns");// N
		String table = json.getString("table");// Y
		String row = json.getString("row");// Y

		String _durability = json.getString("durability");// N
		// Boolean cacheBlocks = json.getBoolean("cacheBlocks");

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
				JSONObject familyJson = (JSONObject) value;

				if (0 == familyJson.size()) {
					families.add(new FamilyVo(key));
				} else if (familyJson.containsKey("timestamp")) {
					families.add(new FamilyVo(key, familyJson.getLong("timestamp"), null));
				} else if (familyJson.containsKey("version")) {
					families.add(new FamilyVo(key, null, familyJson.getLong("version")));
				} else {
					families.add(new FamilyVo(key, parseColumn(familyJson)));
				}
			}
		}

		// check
		HBaseUtil.checkNull(table, "'table' property can not be empty.\n" + text);
		HBaseUtil.checkNull(row, "'row' property can not be empty.\n" + text);

		this.ns = ns;
		this.table = table;
		this.row = row;

		this.durability = parseDurability(_durability);
		// this.cacheBlocks = cacheBlocks;
		this.families = families;
	}

	private List<ColumnVo> parseColumn(JSONObject familyJson) {
		List<ColumnVo> columnList = new ArrayList<ColumnVo>();
		for (Entry<String, Object> item : familyJson.entrySet()) {
			String key = item.getKey();
			Object value = item.getValue();
			// check
			if (!(value instanceof JSONObject)) {
				throw new HBaseCommondParsedException("'family' property structure error.\n" + familyJson);
			}

			JSONObject columnJson = (JSONObject) value;
			if (0 == columnJson.size()) {
				columnList.add(new ColumnVo(key, null, DeleteScope.ALL));
				continue;
			}

			Long timestamp = null;
			if (columnJson.containsKey("timestamp")) {
				timestamp = columnJson.getLong("timestamp");
			}

			DeleteScope scope = DeleteScope.ALL;
			if (columnJson.containsKey("scope")) {
				scope = getDeleteScope(columnJson.getString("scope"));
			}
			columnList.add(new ColumnVo(key, timestamp, scope));
		}
		return columnList;
	}

	private DeleteScope getDeleteScope(String val) {
		if ("ALL".equalsIgnoreCase(val)) {
			return DeleteScope.ALL;
		} else if ("LAST".equalsIgnoreCase(val)) {
			return DeleteScope.LAST;
		}
		throw new HBaseCommondParsedException("The value of attribute 'scope' is wrong: " + val);
	}

	public Delete getHBaseDelete() throws Throwable {

		Delete delete = new Delete(Bytes.toBytes(this.row));

		if (null != this.families) {
			for (FamilyVo familyVo : this.families) {
				if (null == familyVo.getColumns()) {
					if (null == familyVo.getTimestamp() && null == familyVo.getVersion()) {
						delete.addFamily(Bytes.toBytes(familyVo.getName()));
					} else if (null != familyVo.getTimestamp()) {
						delete.addFamily(Bytes.toBytes(familyVo.getName()), familyVo.getTimestamp());
					} else if (null != familyVo.getVersion()) {
						delete.addFamilyVersion(Bytes.toBytes(familyVo.getName()), familyVo.getVersion());
					}
				} else {
					byte[] familyNameBytes = null;
					for (ColumnVo columnVo : familyVo.getColumns()) {
						if (null == familyNameBytes) {
							familyNameBytes = Bytes.toBytes(familyVo.getName());
						}
						if (DeleteScope.ALL == columnVo.getScope()) {
							if (null == columnVo.getTimestamp()) {
								delete.addColumns(familyNameBytes, Bytes.toBytes(columnVo.getName()));
							} else {
								delete.addColumns(familyNameBytes, Bytes.toBytes(columnVo.getName()), columnVo.getTimestamp());
							}
						} else {
							if (null == columnVo.getTimestamp()) {
								delete.addColumn(familyNameBytes, Bytes.toBytes(columnVo.getName()));
							} else {
								delete.addColumn(familyNameBytes, Bytes.toBytes(columnVo.getName()), columnVo.getTimestamp());
							}
						}
					}
				}
			}
		}

		if (null != this.durability) {
			delete.setDurability(this.durability);
		}

		return delete;
	}

}
