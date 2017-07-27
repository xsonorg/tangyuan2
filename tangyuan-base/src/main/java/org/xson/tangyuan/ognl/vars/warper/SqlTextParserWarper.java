package org.xson.tangyuan.ognl.vars.warper;

import java.util.ArrayList;
import java.util.List;

import org.xson.tangyuan.ognl.vars.ParserWarper;
import org.xson.tangyuan.ognl.vars.VariableConfig;

/**
 * SQL text解析包装
 */
public class SqlTextParserWarper {

	private List<Object> parse0(String text, VariableConfig config) {
		String openToken = config.getOpenToken();
		String closeToken = config.getCloseToken();
		ParserWarper warper = config.getWarper();
		List<Object> list = new ArrayList<Object>();
		StringBuilder builder = new StringBuilder();
		if (text != null && text.length() > 0) {
			char[] src = text.toCharArray();
			int offset = 0;
			int start = text.indexOf(openToken, offset);
			while (start > -1) {
				if (start > 0 && src[start - 1] == '\\') {
					// the variable is escaped. remove the backslash.
					builder.append(src, offset, start - 1).append(openToken);
					offset = start + openToken.length();
				} else {
					int end = text.indexOf(closeToken, start);
					if (end == -1) {
						builder.append(src, offset, src.length - offset);
						offset = src.length;
					} else {
						builder.append(src, offset, start - offset);
						// 记录之前的字符串
						list.add(builder.toString());
						builder = new StringBuilder();
						offset = start + openToken.length();

						int nestedEnd = 0;
						if (config.isAllowNested()) {
							nestedEnd = warper.getNestedParser().check(text, offset, end);
						}
						if (nestedEnd > 0) {
							end = nestedEnd;
							config.setExistNested(true);// 存在嵌套
						}
						String content = new String(src, offset, end - offset);
						list.add(warper.parse(content, config));
						offset = end + closeToken.length();
					}
				}
				start = text.indexOf(openToken, offset);
			}
			if (offset < src.length) {
				builder.append(src, offset, src.length - offset);
				list.add(builder.toString());// 记录
			}
		}
		return list;
	}

	public List<Object> parse(String text, VariableConfig[] configs) {
		List<Object> allList = new ArrayList<Object>();
		allList.add(text);
		List<Object> eachList = new ArrayList<Object>();
		for (int i = 0; i < configs.length; i++) {
			for (Object item : allList) {
				if (item instanceof String) {
					List<Object> tempList = parse0((String) item, configs[i]);
					if (tempList.size() > 0) {
						eachList.addAll(tempList);
					}
				} else {
					eachList.add(item);
				}
			}
			allList = eachList;
			eachList = new ArrayList<Object>();
		}
		return allList;
	}
}
