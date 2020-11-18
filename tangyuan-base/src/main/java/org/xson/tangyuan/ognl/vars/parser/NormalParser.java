package org.xson.tangyuan.ognl.vars.parser;

import java.util.ArrayList;
import java.util.List;

import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.ognl.vars.vo.NormalVariable;
import org.xson.tangyuan.ognl.vars.vo.VariableItem;
import org.xson.tangyuan.ognl.vars.vo.VariableItemWraper;
import org.xson.tangyuan.xml.nsarg.XmlExtNsArg;

public class NormalParser extends AbstractParser {

	/**
	 * 是否只是简单的属性
	 */
	protected boolean isSimpleProperty(String text) {
		int dotIndex            = text.indexOf(".");
		int squareBracketsIndex = text.indexOf("[");
		if (dotIndex < 0 && squareBracketsIndex < 0) {
			return true;
		}
		return false;
	}

	private VariableItem getItemUnit(StringBuilder builder, boolean isInternalProperty) {
		String property = builder.toString().trim();
		if (!isInternalProperty) {
			return new VariableItem(property, false);
		} else {
			// 索引
			boolean isNum = isInteger(property);
			if (isNum) {
				return new VariableItem(Integer.parseInt(property));
			} else {
				if (isStaticString(property)) {
					property = property.substring(1, property.length() - 1);
					return new VariableItem(property, false);
				}
				return new VariableItem(property, true);// 动态的
			}
		}
	}

	/**
	 * 解析属性: p.a.b['xx' ][0].c[x]
	 */
	private List<VariableItem> parseProperty0(String text) {
		List<VariableItem> list               = new ArrayList<VariableItem>();
		int                srcLength          = text.length();
		StringBuilder      builder            = new StringBuilder();
		boolean            isInternalProperty = false; // 是否进入内部属性采集
		for (int i = 0; i < srcLength; i++) {
			char key = text.charAt(i);
			switch (key) {
			case '.': // 前面采集告一段落
				if (builder.length() > 0) {
					list.add(getItemUnit(builder, isInternalProperty));
					builder = new StringBuilder();
				}
				break;
			case '[': // 进入括弧模式
				if (builder.length() > 0) {
					list.add(getItemUnit(builder, isInternalProperty));
					builder = new StringBuilder();
				}
				isInternalProperty = true;
				break;
			case ']':
				if (builder.length() > 0) {
					list.add(getItemUnit(builder, isInternalProperty));
					builder = new StringBuilder();
				}
				isInternalProperty = false;
				break; // 退出括弧模式
			// case '\'': case '"': 这里不做处理
			default:
				builder.append(key);
			}
		}
		if (builder.length() > 0) {
			list.add(getItemUnit(builder, isInternalProperty));
		}
		return list;
	}

	@Override
	public Variable parse(String text) {
		//		text = text.trim();
		//		if (isSimpleProperty(text)) {
		//			return new NormalVariable(text, new VariableItemWraper(text, new VariableItem(text, false)));
		//		}
		//		List<VariableItem> itemList = parseProperty0(text);
		//		return new NormalVariable(text, new VariableItemWraper(text, itemList));

		text = text.trim();
		//		String prefix = XmlExtNsArg.getInstance().isExtProperty(text);
		String prefix = XmlExtNsArg.getInstance().isExtNs(text);
		if (null != prefix) {
			text = text.substring(prefix.length());
		}

		if (isSimpleProperty(text)) {
			return new NormalVariable(text, new VariableItemWraper(text, prefix, new VariableItem(text, false)));
		}
		List<VariableItem> itemList = parseProperty0(text);
		return new NormalVariable(text, new VariableItemWraper(text, prefix, itemList));

	}

}
