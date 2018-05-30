package org.xson.tangyuan.web.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.xson.tangyuan.web.RequestContext.RequestTypeEnum;

public class RestUtil {

	public static String[] splitToStringArray(String str, String delimiter) {
		Collection<String> collection = splitToStringList(str, delimiter);
		if (collection == null || 0 == collection.size()) {
			return null;
		}
		return collection.toArray(new String[collection.size()]);
	}

	public static List<String> splitToStringList(String str, String delimiter) {
		List<String> result = new ArrayList<String>();
		int pos = 0;
		int delPos;
		while ((delPos = str.indexOf(delimiter, pos)) != -1) {
			result.add(str.substring(pos, delPos));
			pos = delPos + delimiter.length();
		}
		if (str.length() > 0 && pos <= str.length()) {
			result.add(str.substring(pos));
		}
		if (0 == result.size()) {
			return null;
		}
		return result;
	}

	public static String getRestKey(RequestTypeEnum requestType, String path) {
		return requestType.toString() + " " + path;
	}

	
	// public static String getRestKey(String requestType, String path) {
	// return requestType + " " + path;
	// }
	// public void parseCurrentURI(String uri) {
	//
	// }
	// public void parseDefinedURI(String uri) {
	// // StringBuilder sb = new StringBuilder();
	// // 0. 是否包含变量
	// // 1. 结构数
	// // 2. 模板 /zoos/#/animals/#
	// // 3. 变量 ID, ID
	// // 4. 变量的位置 1, 3
	// // uri.split(",");
	// }
	// public static void main(String[] args) {
	// // API/USER/{xxxx}
	// // /zoos/{ID}/animals/{ID}
	// // /zoos/123/animals/456
	// // String pattern = "/zoos/{AID}/animals/{BID}";
	// String pattern = "/zoos/*/animals/*";
	// String str = "/zoos/1*23/animals/45/6";
	// System.out.println(PatternMatchUtils.simpleMatch(pattern, str));
	// AntPathMatcher apm = new AntPathMatcher();
	// System.out.println(apm.match(pattern, str));
	// }
}
