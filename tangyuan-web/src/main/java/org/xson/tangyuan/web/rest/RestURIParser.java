package org.xson.tangyuan.web.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.tangyuan.web.util.RestUtil;
import org.xson.tangyuan.web.util.ServletUtils;
import org.xson.tangyuan.xml.XmlParseException;

public class RestURIParser {

	private boolean checkPathVar(String str) {
		if (null != str && str.length() > 2 && str.startsWith("{") && str.endsWith("}")) {
			return true;
		}
		return false;
	}

	private boolean checkQueryVar(String str) {
		if (null != str && str.length() >= 2 && str.startsWith("{") && str.endsWith("}")) {
			return true;
		}
		return false;
	}

	private String getVar(String str) {
		return str.substring(1, str.length() - 1).trim();
	}

	private Map<String, String> parseQueryString(String query, String uri) {
		// page=2&per_page=100
		if (0 == query.length()) {
			return null;
		}
		List<String> itemList = RestUtil.splitToStringList(query, RestURIVo.URI_SYMBOL_AND);
		if (null == itemList || 0 == itemList.size()) {
			return null;
		}
		// key: originalVarName, value:customVarName
		Map<String, String> queryVariables = new HashMap<String, String>();

		int size = itemList.size();
		for (int i = 0; i < size; i++) {
			String item = itemList.get(i);
			int pos = item.indexOf(RestURIVo.URI_SYMBOL_EQUAL);
			if (pos < 0) {
				throw new XmlParseException("Invalid query string. uri: " + uri);
			}
			String name = item.substring(0, pos);
			String value = item.substring(pos + 1);
			if (!checkQueryVar(value)) {
				throw new XmlParseException("Query string variable format is illegal, it should be {XXX}. uri: " + uri);
			}
			String realVal = getVar(value);
			if (0 == realVal.length()) {
				realVal = name;
			}
			if (queryVariables.containsKey(name)) {
				throw new XmlParseException("Duplicate variable name in query string. uri: " + uri);
			}
			queryVariables.put(name, realVal);
		}
		if (0 == queryVariables.size()) {
			return null;
		}
		return queryVariables;
	}

	public RestURIVo parseURI(String uri) {
		/* /xx1/{id}/animals?page={}&per_page={per_page} */

		RestURIVo restURI = null;
		// key: pos, value:varName
		Map<Integer, String> pathVariables = null;
		// key: originalVarName, value:customVarName
		Map<String, String> queryVariables = null;

		int pos = uri.indexOf(RestURIVo.URI_SYMBOL_QUESTION_MARK);// ?
		String path = uri;
		if (pos > 0) {
			path = uri.substring(0, pos);
			queryVariables = parseQueryString(uri.substring(pos + 1), uri);
		}
		pos = uri.indexOf("{");
		if (pos > 0) {// 存在变量
			// 路径单元分隔的时候去掉'/'
			// String rPath = path;
			// if (rPath.length() > 1 && rPath.startsWith(RestURIVo.URI_SYMBOL_FOLDER_SEPARATOR)) {
			// rPath = path.substring(1);
			// }
			// List<String> itemList = RestUtil.splitToStringList(rPath, RestURIVo.URI_SYMBOL_FOLDER_SEPARATOR);// /
			// item首元素为'/'

			List<String> itemList = ServletUtils.parseURIPathItem(path);

			pathVariables = new HashMap<Integer, String>();
			List<String> patternList = new ArrayList<String>();

			int size = itemList.size();
			for (int i = 0; i < size; i++) {
				String item = itemList.get(i);
				if (checkPathVar(item)) {
					String varName = getVar(item);
					if ((null != queryVariables && queryVariables.containsKey(varName)) || pathVariables.containsValue(varName)) {
						throw new XmlParseException("Duplicate variable name in query string. uri: " + uri);
					}
					pathVariables.put(i, varName);
					patternList.add(RestURIVo.URI_SYMBOL_HASHTAG); // add #
				} else {
					patternList.add(item);
				}
			}
			restURI = new RestURIVo(path, patternList, pathVariables, queryVariables);
		} else {// 没有变量
			restURI = new RestURIVo(path, null, pathVariables, queryVariables);
		}

		return restURI;
	}

}
