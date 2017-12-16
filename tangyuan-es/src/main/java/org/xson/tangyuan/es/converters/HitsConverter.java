package org.xson.tangyuan.es.converters;

import java.util.ArrayList;
import java.util.List;

import org.xson.common.object.XCO;
import org.xson.tangyuan.es.ResultConverter;
import org.xson.tangyuan.es.util.FastJsonUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class HitsConverter implements ResultConverter {

	public static String key = "@hits";

	@Override
	public Object convert(String json) {
		JSONObject jsonObject = JSON.parseObject(json);

		JSONObject hits = jsonObject.getJSONObject("hits");
		int total = hits.getIntValue("total");
		// TODO check

		List<XCO> list = new ArrayList<XCO>();

		XCO xco = new XCO();
		xco.setIntegerValue("total", total);
		if (total > 0) {
			JSONArray hitsArray = hits.getJSONArray("hits");
			int length = hitsArray.size();
			for (int i = 0; i < length; i++) {
				XCO item = new XCO();
				JSONObject hit = hitsArray.getJSONObject(i);
				item.setStringValue("_id", hit.getString("_id"));
				JSONObject _source = hit.getJSONObject("_source");
				if (null != _source) {
					FastJsonUtil.toXCO(item, _source);
				}
				list.add(item);
			}
		}
		xco.setXCOListValue("hits", list);
		return xco;
	}

}
