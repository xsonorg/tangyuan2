package org.xson.tangyuan.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.ognl.vars.ArgSelfVo;
import org.xson.tangyuan.ognl.vars.parser.NormalParser;

/**
 * XML中文本解析工具
 */
public class XmlTextParseUtil {

	private static char escapeFlag = '\\';

	/**
	 * 是否是静态字符串
	 */
	public static boolean isStaticString(String text) {
		if (text.length() > 1 && ((text.startsWith("'") && text.endsWith("'")) || (text.startsWith("\"") && text.endsWith("\"")))) {
			return true;
		}
		return false;
	}

	public static boolean isNumber(String text) {
		return text.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)[lLfFdD]?$");
	}

	/**
	 * 是否是数值后缀
	 */
	public static boolean isNumberSuffix(char chr) {
		if (chr == 'l' || chr == 'L' || chr == 'f' || chr == 'F' || chr == 'd' || chr == 'D') {
			return true;
		}
		return false;
	}

	public static Object parseNumber(String text) {
		char chr = text.charAt(text.length() - 1);
		if (isNumberSuffix(chr)) {
			if (chr == 'l' || chr == 'L') {
				return Long.valueOf(text.substring(0, text.length() - 1));
			} else if (chr == 'f' || chr == 'F') {
				return Float.valueOf(text.substring(0, text.length() - 1));
			} else if (chr == 'd' || chr == 'D') {
				return Double.valueOf(text.substring(0, text.length() - 1));
			}
		} else if (text.indexOf(".") > -1) {
			Object number = null;
			try {
				number = Float.parseFloat(text);
			} catch (NumberFormatException e) {
				number = Double.parseDouble(text);
			}
			return number;
		} else {
			Object number = null;
			try {
				number = Integer.parseInt(text);
			} catch (NumberFormatException e) {
				number = Long.parseLong(text);
			}
			return number;
		}
		throw new TangYuanException("Illegal numeric string: " + text);
	}

	public static int findMatchedChar(String context, char chrFlag) {
		return findMatchedChar(context, 0, context.length(), chrFlag);
	}

	public static int findMatchedChar(String context, int start, char chrFlag) {
		return findMatchedChar(context, start, context.length(), chrFlag);
	}

	public static int findMatchedChar(String context, int start, int end, char chrFlag) {
		char    chr;
		boolean stringMode             = false;
		boolean stringModeSingleQuotes = false;
		boolean stringModeDoubleQuotes = false;
		for (int i = start; i < end; i++) {
			chr = context.charAt(i);
			switch (chr) {
			case '\'':
				if (stringMode) {
					if (stringModeSingleQuotes && (0 == i || escapeFlag != context.charAt(i - 1))) {
						stringModeSingleQuotes = false;
						stringMode = false;
					}
				} else {
					if (0 == i || escapeFlag != context.charAt(i - 1)) {
						stringModeSingleQuotes = true;
						stringMode = true;
					}
				}
				break;
			case '"':
				if (stringMode) {
					if (stringModeDoubleQuotes && (0 == i || escapeFlag != context.charAt(i - 1))) {
						stringModeDoubleQuotes = false;
						stringMode = false;
					}
				} else {
					if (0 == i || escapeFlag != context.charAt(i - 1)) {
						stringModeDoubleQuotes = true;
						stringMode = true;
					}
				}
				break;
			default:
				if (stringMode) {
					break;
				}
				if (chr == chrFlag && (0 == i || escapeFlag != context.charAt(i - 1))) {
					return i;
				}
				break;
			}
		}
		return -1;
	}

	public static int findNestedMatchedChar(String context, int start, char startChar, char endChar) {
		char    chr;
		boolean stringMode             = false;
		boolean stringModeSingleQuotes = false;
		boolean stringModeDoubleQuotes = false;
		int     count                  = 0;
		for (int i = start + 1; i < context.length(); i++) {
			chr = context.charAt(i);
			switch (chr) {
			case '\'':
				if (stringMode) {
					if (stringModeSingleQuotes && (escapeFlag != context.charAt(i - 1))) {
						stringModeSingleQuotes = false;
						stringMode = false;
					}
				} else {
					if (escapeFlag != context.charAt(i - 1)) {
						stringModeSingleQuotes = true;
						stringMode = true;
					}
				}
				break;
			case '"':
				if (stringMode) {
					if (stringModeDoubleQuotes && (escapeFlag != context.charAt(i - 1))) {
						stringModeDoubleQuotes = false;
						stringMode = false;
					}
				} else {
					if (escapeFlag != context.charAt(i - 1)) {
						stringModeDoubleQuotes = true;
						stringMode = true;
					}
				}
				break;
			default:
				if (stringMode) {
					break;
				}
				if (chr == startChar && (escapeFlag != context.charAt(i - 1))) {
					count++;
				} else if (chr == endChar && (escapeFlag != context.charAt(i - 1))) {
					if (count == 0) {
						return i;
					} else {
						count--;
					}
				}
			}
		}
		return -1;
	}

	//

	/**
	 * 查找关键字的最后一个位置 
	 * 
	 * @param context
	 * @param start n->pos
	 * @param keys { "new", "Date", "(" }
	 * @return  pos->'(' 
	 */
	public static int findKeysEndPos(String context, int start, String[] keys) {
		int length    = context.length();
		int keysIndex = 0;
		for (int i = start; i < length; i++) {
			// skip
			char cur = context.charAt(i);
			if (Character.isWhitespace(cur)) {
				continue;
			}
			// safe
			int end = i + keys[keysIndex].length();
			if (end >= length) {
				end = length;
			}
			String key = context.substring(i, end);
			if (!keys[keysIndex].equals(key)) {
				return -1;
			}
			i = i + keys[keysIndex].length() - 1;//指到最后
			keysIndex++;
			if (keysIndex == keys.length) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 分割字符串
	 * @param src
	 * @param separator
	 * @return
	 */
	public static List<String> splitToList(String context, char separator) {
		if (StringUtils.isEmptySafe(context)) {
			return null;
		}
		List<String>  temp                   = new ArrayList<String>();
		StringBuilder sb                     = new StringBuilder();
		boolean       stringMode             = false;
		boolean       stringModeSingleQuotes = false;
		boolean       stringModeDoubleQuotes = false;

		for (int i = 0; i < context.length(); i++) {
			char key = context.charAt(i);
			switch (key) {
			case '\'':
				if (stringMode) {
					if (stringModeSingleQuotes && (0 == i || escapeFlag != context.charAt(i - 1))) {
						stringModeSingleQuotes = false;
						stringMode = false;
					}
				} else {
					if (0 == i || escapeFlag != context.charAt(i - 1)) {
						stringModeSingleQuotes = true;
						stringMode = true;
					}
				}
				sb.append(key);
				break;
			case '"':
				if (stringMode) {
					if (stringModeDoubleQuotes && (0 == i || escapeFlag != context.charAt(i - 1))) {
						stringModeDoubleQuotes = false;
						stringMode = false;
					}
				} else {
					if (0 == i || escapeFlag != context.charAt(i - 1)) {
						stringModeDoubleQuotes = true;
						stringMode = true;
					}
				}
				sb.append(key);
				break;
			default:
				if (separator == key && !stringMode) {//TODO escapeFlag
					if (sb.length() > 0) {
						temp.add(sb.toString());
						sb = new StringBuilder();
					}
				} else {
					sb.append(key);
				}
				break;
			}
		}
		if (sb.length() > 0) {
			temp.add(sb.toString());
		}

		if (CollectionUtils.isEmpty(temp)) {
			return null;
		}
		return temp;
	}

	public static String[] splitToArray(String src, char separator) {
		List<String> list = splitToList(src, separator);
		if (CollectionUtils.isEmpty(list)) {
			return null;
		}
		String[] array = new String[list.size()];
		return list.toArray(array);
	}

	public static Object parseTextValue(String text) {
		text = StringUtils.trimEmpty(text);
		if (null == text) {
			return null;
		}
		if ("null".equalsIgnoreCase(text)) {
			return null;
		}
		// boolean
		if ("true".equalsIgnoreCase(text) || "false".equalsIgnoreCase(text)) {
			return Boolean.valueOf(text);
		}
		// 字符串
		if (isStaticString(text)) {
			return text.substring(1, text.length() - 1);
		}
		// 数值
		if (isNumber(text)) {
			return parseNumber(text);
		}
		// self
		if ("*".equals(text) || ArgSelfVo.AEG_SELF_MARK.equalsIgnoreCase(text)) {
			return ArgSelfVo.argSelf;
		}
		return new NormalParser().parse(text);
	}

	public static void main(String[] args) {
		//		String   str1 = "ccc, 'a,b', 0L";
		//		String   str2 = ",,,,";
		//		String   str3 = "ccc, \"a,b\", 0L";
		//		String   str4 = "ccc, \"a'\\\",b\", 0L";

		String[] strs = { "ccc, 'a,b', 0L", ",,,,", "ccc, \"a,b\", 0L", "ccc, \"a'\",b\", 0L", "ccc, 'a\",b', 0L" };
		for (int i = 0; i < strs.length; i++) {
			String       str = strs[i];
			List<String> a   = splitToList(str, ',');
			List<String> b   = Arrays.asList(str.split(","));
			if (null == a) {
				a = new ArrayList<String>();
			}
			if (null == b) {
				b = new ArrayList<String>();
			}
			System.out.println("src[" + str + "]" + "\t" + a.toString() + ":" + a.size() + "\t" + b.toString() + ":" + b.size());
		}

		System.out.println(",".split(",").length);

	}

	//	String[] result = new String[temp.size()];
	//	return temp.toArray(result);
	//			case '\'':
	//				isString = !isString;
	//				sb.append(key);
	//				break;
	//			case '"':
	//				break;
}
