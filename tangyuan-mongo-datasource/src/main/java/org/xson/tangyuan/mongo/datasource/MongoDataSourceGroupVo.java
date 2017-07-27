package org.xson.tangyuan.mongo.datasource;

import java.util.Map;

public class MongoDataSourceGroupVo extends MongoDataSourceVo {

	private int	start;
	private int	end;
	private int	count;

	public MongoDataSourceGroupVo(String id, boolean defaultDs, Map<String, String> properties, String creator, int start, int end) {
		// super(id, properties, defaultDs);
		super(id, properties, defaultDs, null, creator);
		this.start = start;
		this.end = end;
		this.count = this.end - this.start + 1;
		this.group = true;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public int getCount() {
		return count;
	}

}
