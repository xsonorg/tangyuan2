package com.mongodb.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.SimpleTimeZone;

import org.bson.types.BSONTimestamp;
import org.bson.types.Code;
import org.bson.types.Decimal128;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.mongo.MongoComponent;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.ognl.vars.warper.GAParserWarper;
import org.xson.tangyuan.util.NumberUtils;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.util.XmlTextParseUtil;

public class ParserExt {

	interface SpecialParser {
		ParserExtResult parse(String src, int pos) throws Throwable;
	}

	class ParserExtResult {
		/** 结束的位置*/
		int    endPos;
		Object value;

		public ParserExtResult(int endPos, Object value) {
			super();
			this.endPos = endPos;
			this.value = value;
		}
	}

	//	private char escapeFlag = '\\';
	//	private int findMatchedChar(String context, int start, char startChar, char endChar) {
	//		char    chr;
	//		boolean stringMode             = false;
	//		boolean stringModeSingleQuotes = false;
	//		boolean stringModeDoubleQuotes = false;
	//		int     count                  = 0;
	//		for (int i = start + 1; i < context.length(); i++) {
	//			chr = context.charAt(i);
	//			switch (chr) {
	//			case '\'':
	//				if (stringMode) {
	//					if (stringModeSingleQuotes && (escapeFlag != context.charAt(i - 1))) {
	//						stringModeSingleQuotes = false;
	//						stringMode = false;
	//					}
	//				} else {
	//					if (escapeFlag != context.charAt(i - 1)) {
	//						stringModeSingleQuotes = true;
	//						stringMode = true;
	//					}
	//				}
	//				break;
	//			case '"':
	//				if (stringMode) {
	//					if (stringModeDoubleQuotes && (escapeFlag != context.charAt(i - 1))) {
	//						stringModeDoubleQuotes = false;
	//						stringMode = false;
	//					}
	//				} else {
	//					if ('\\' != context.charAt(i - 1)) {
	//						stringModeDoubleQuotes = true;
	//						stringMode = true;
	//					}
	//				}
	//				break;
	//			default:
	//				if (stringMode) {
	//					break;
	//				}
	//				if (chr == startChar && (escapeFlag != context.charAt(i - 1))) {
	//					count++;
	//				} else if (chr == endChar && (escapeFlag != context.charAt(i - 1))) {
	//					if (count == 0) {
	//						return i;
	//					} else {
	//						count--;
	//					}
	//				}
	//			}
	//		}
	//		return -1;
	//	}

	/**
	 * 查找关键字的最后一个位置
	 */
	//	private int findKeysEndPos(String src, int pos, String[] keys) {
	//		int length    = src.length();
	//		int keysIndex = 0;
	//		for (int i = pos; i < length; i++) {
	//			// skip
	//			char cur = src.charAt(i);
	//			if (Character.isWhitespace(cur)) {
	//				continue;
	//			}
	//			// safe
	//			int end = i + keys[keysIndex].length();
	//			if (end >= length) {
	//				end = length;
	//			}
	//			String key = src.substring(i, end);
	//			if (!keys[keysIndex].equals(key)) {
	//				return -1;
	//			}
	//			i = i + keys[keysIndex].length() - 1;//指到最后
	//			keysIndex++;
	//			if (keysIndex == keys.length) {
	//				return i;
	//			}
	//		}
	//		return -1;
	//	}

	//	protected boolean isString(String text) {
	//		if (text.length() >= 2 && ((text.startsWith("'") && text.endsWith("'")) || (text.startsWith("\"") && text.endsWith("\"")))) {
	//			return true;
	//		}
	//		return false;
	//	}

	////////////////////////////////////////////////////////// -->n

	protected class NullParser implements SpecialParser {

		private String[] keys = { "null" };

		@Override
		public ParserExtResult parse(String src, int pos) throws Throwable {
			int keysEndPos = XmlTextParseUtil.findKeysEndPos(src, pos, keys);
			if (-1 == keysEndPos) {
				return null;
			}
			return new ParserExtResult(keysEndPos, null);
		}
	}

	protected class NewDateParser implements SpecialParser {

		private String[] keys = { "new", "Date", "(" };

		@Override
		public ParserExtResult parse(String src, int pos) throws Throwable {
			int keysEndPos = XmlTextParseUtil.findKeysEndPos(src, pos, keys);
			if (-1 == keysEndPos) {
				return null;
			}
			int nextEndPos = src.indexOf(")", keysEndPos + 1);
			if (-1 == nextEndPos) {
				return null;
			}

			String content = src.substring(keysEndPos + 1, nextEndPos);
			content = StringUtils.trimEmpty(content);

			Object value = null;
			if (null == content) {
				value = new Date();
			} else if (NumberUtils.isInteger(content)) {
				value = new Date(Long.parseLong(content));
			} else if (XmlTextParseUtil.isStaticString(content)) {
				content = content.substring(1, content.length() - 1);
				value = JSONExtCallback.parseISODate(content);
			}

			//			System.out.println("###" + content);
			//			return new ParserExtResult(nextEndPos + 1, value);
			return new ParserExtResult(nextEndPos, value);
			//			System.out.println(src.charAt(pos));
			//			System.out.println(src.substring(pos, keysEndPos));
			//			System.out.println(src.substring(pos, keysEndPos + 1));
		}
	}

	protected class NewTimestampParser implements SpecialParser {

		private String[] keys = { "new", "Timestamp", "(" };

		@Override
		public ParserExtResult parse(String src, int pos) throws Throwable {
			int keysEndPos = XmlTextParseUtil.findKeysEndPos(src, pos, keys);
			if (-1 == keysEndPos) {
				return null;
			}
			int nextEndPos = src.indexOf(")", keysEndPos + 1);
			if (-1 == nextEndPos) {
				return null;
			}

			String content = src.substring(keysEndPos + 1, nextEndPos);
			content = StringUtils.trimEmpty(content);

			Object value = null;
			if (null == content) {
				value = new BSONTimestamp();
			} else if (NumberUtils.isInteger(content)) {
				value = new BSONTimestamp(Integer.parseInt(content), 0);
			} else {
				String[] array = content.split(",");
				value = new BSONTimestamp(Integer.parseInt(StringUtils.trim(array[0])), Integer.parseInt(StringUtils.trim(array[1])));
			}
			//			return new ParserExtResult(nextEndPos + 1, value);
			return new ParserExtResult(nextEndPos, value);
		}
	}

	//////////////////////////////////////////////////////////-->N

	// NaN
	protected class NaNParser implements SpecialParser {

		private String[] keys = { "NaN" };

		@Override
		public ParserExtResult parse(String src, int pos) throws Throwable {
			int keysEndPos = XmlTextParseUtil.findKeysEndPos(src, pos, keys);
			if (-1 == keysEndPos) {
				return null;
			}
			return new ParserExtResult(keysEndPos, null);
		}
	}

	// NumberLong
	protected class NumberLongParser implements SpecialParser {

		private String[] keys = { "NumberLong", "(" };

		@Override
		public ParserExtResult parse(String src, int pos) throws Throwable {
			int keysEndPos = XmlTextParseUtil.findKeysEndPos(src, pos, keys);
			if (-1 == keysEndPos) {
				return null;
			}
			int nextEndPos = src.indexOf(")", keysEndPos + 1);
			if (-1 == nextEndPos) {
				return null;
			}

			String content = src.substring(keysEndPos + 1, nextEndPos);
			content = StringUtils.trimEmpty(content);
			if (XmlTextParseUtil.isStaticString(content)) {
				content = content.substring(1, content.length() - 1);
			}

			Object value = Long.parseLong(content);
			return new ParserExtResult(nextEndPos, value);
		}
	}

	// NumberInt
	protected class NumberIntParser implements SpecialParser {

		private String[] keys = { "NumberInt", "(" };

		@Override
		public ParserExtResult parse(String src, int pos) throws Throwable {
			int keysEndPos = XmlTextParseUtil.findKeysEndPos(src, pos, keys);
			if (-1 == keysEndPos) {
				return null;
			}
			int nextEndPos = src.indexOf(")", keysEndPos + 1);
			if (-1 == nextEndPos) {
				return null;
			}

			String content = src.substring(keysEndPos + 1, nextEndPos);
			content = StringUtils.trimEmpty(content);
			if (XmlTextParseUtil.isStaticString(content)) {
				content = content.substring(1, content.length() - 1);
			}

			Object value = Integer.parseInt(content);
			return new ParserExtResult(nextEndPos, value);
		}
	}

	// NumberDecimal
	protected class NumberDecimalParser implements SpecialParser {

		private String[] keys = { "NumberDecimal", "(" };

		@Override
		public ParserExtResult parse(String src, int pos) throws Throwable {
			int keysEndPos = XmlTextParseUtil.findKeysEndPos(src, pos, keys);
			if (-1 == keysEndPos) {
				return null;
			}
			int nextEndPos = src.indexOf(")", keysEndPos + 1);
			if (-1 == nextEndPos) {
				return null;
			}

			String content = src.substring(keysEndPos + 1, nextEndPos);
			content = StringUtils.trimEmpty(content);
			if (XmlTextParseUtil.isStaticString(content)) {
				content = content.substring(1, content.length() - 1);
			}

			Object value = Decimal128.parse(content);
			return new ParserExtResult(nextEndPos, value);
		}
	}

	////////////////////////////////////////////////////////// -->I
	protected class ISODateParser implements SpecialParser {

		private String[] keys = { "ISODate", "(" };

		@Override
		public ParserExtResult parse(String src, int pos) throws Throwable {
			int keysEndPos = XmlTextParseUtil.findKeysEndPos(src, pos, keys);
			if (-1 == keysEndPos) {
				return null;
			}
			int nextEndPos = src.indexOf(")", keysEndPos + 1);
			if (-1 == nextEndPos) {
				return null;
			}

			String content = src.substring(keysEndPos + 1, nextEndPos);
			content = StringUtils.trimEmpty(content);

			if (XmlTextParseUtil.isStaticString(content)) {
				content = content.substring(1, content.length() - 1);
			}

			Object value = JSONExtCallback.parseISODate(content);
			return new ParserExtResult(nextEndPos, value);
		}
	}

	////////////////////////////////////////////////////////// -->D
	protected class DateParser implements SpecialParser {

		private String[] keys = { "Date", "(" };

		@Override
		public ParserExtResult parse(String src, int pos) throws Throwable {
			int keysEndPos = XmlTextParseUtil.findKeysEndPos(src, pos, keys);
			if (-1 == keysEndPos) {
				return null;
			}
			int nextEndPos = src.indexOf(")", keysEndPos + 1);
			if (-1 == nextEndPos) {
				return null;
			}
			//			Object value = new Date().toString();
			//			System.out.println(value);
			//			System.out.println(new Date().toGMTString());
			//			System.out.println(new Date().toLocaleString());

			//SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
			String pattern = MongoComponent.getInstance().getDefaultMongoDatePattern();
			Object value   = new SimpleDateFormat(pattern, Locale.US).format(new Date());
			return new ParserExtResult(nextEndPos, value);
		}
	}

	////////////////////////////////////////////////////////// -->false
	protected class FalseParser implements SpecialParser {

		private String[] keys = { "false" };

		@Override
		public ParserExtResult parse(String src, int pos) throws Throwable {
			int keysEndPos = XmlTextParseUtil.findKeysEndPos(src, pos, keys);
			if (-1 == keysEndPos) {
				return null;
			}
			return new ParserExtResult(keysEndPos, false);
		}
	}

	protected class FunctionParser implements SpecialParser {

		private String[] keys = { "function", "(" };

		@Override
		public ParserExtResult parse(String src, int pos) throws Throwable {
			int keysEndPos = XmlTextParseUtil.findKeysEndPos(src, pos, keys);
			if (-1 == keysEndPos) {
				return null;
			}
			int nextEndPos = src.indexOf(")", keysEndPos + 1);
			if (-1 == nextEndPos) {
				return null;
			}

			nextEndPos = src.indexOf("{", keysEndPos + 1);
			if (-1 == nextEndPos) {
				return null;
			}
			//			nextEndPos = src.indexOf("}", keysEndPos + 1);
			//			nextEndPos = findMatchedChar(src, nextEndPos, '{', '}');
			nextEndPos = XmlTextParseUtil.findNestedMatchedChar(src, nextEndPos, '{', '}');
			if (-1 == nextEndPos) {
				return null;
			}

			String content = src.substring(pos, nextEndPos + 1);
			//			FunctionCode code    = new FunctionCode(content);
			Code   code    = new Code(content);
			//			System.out.println("==================================\n\n" + content);

			return new ParserExtResult(nextEndPos, code);
		}
	}

	// @
	//	protected class AtParser implements SpecialParser {
	//
	//		private String[] keys = { "@", "{" };
	//
	//		@Override
	//		public ParserExtResult parse(String src, int pos) throws Throwable {
	//			int keysEndPos = findKeysEndPos(src, pos, keys);
	//			if (-1 == keysEndPos) {
	//				return null;
	//			}
	//			int nextEndPos = XmlTextParseUtil.findNestedMatchedChar(src, keysEndPos, '{', '}');
	//			if (-1 == nextEndPos) {
	//				return null;
	//			}
	//			String content = src.substring(pos + 2, nextEndPos);
	//			return new ParserExtResult(nextEndPos, code);
	//		}
	//	}

	private List<SpecialParser> nParserList = new ArrayList<SpecialParser>();
	private List<SpecialParser> NParserList = new ArrayList<SpecialParser>();
	private List<SpecialParser> IParserList = new ArrayList<SpecialParser>();
	private List<SpecialParser> DParserList = new ArrayList<SpecialParser>();
	private List<SpecialParser> fParserList = new ArrayList<SpecialParser>();

	protected ParserExt() {
		// n
		nParserList.add(new NullParser());
		nParserList.add(new NewDateParser());
		nParserList.add(new NewTimestampParser());

		// N
		NParserList.add(new NaNParser());
		NParserList.add(new NumberIntParser());
		NParserList.add(new NumberLongParser());
		NParserList.add(new NumberDecimalParser());

		// I
		IParserList.add(new ISODateParser());

		// D
		DParserList.add(new DateParser());

		// f
		fParserList.add(new FalseParser());
		fParserList.add(new FunctionParser());

		// @
		//		atParserList.add(new AtParser());
	}

	protected ParserExtResult parseN(String src, int pos) {
		return parse0(src, pos, nParserList, "n");
	}

	protected ParserExtResult parseNN(String src, int pos) {
		return parse0(src, pos, NParserList, "N");
	}

	protected ParserExtResult parseI(String src, int pos) {
		return parse0(src, pos, IParserList, "I");
	}

	protected ParserExtResult parseD(String src, int pos) {
		return parse0(src, pos, DParserList, "D");
	}

	protected ParserExtResult parsef(String src, int pos) {
		return parse0(src, pos, fParserList, "f");
	}

	protected ParserExtResult parseCall(String src, int pos, JSONExtCallback callback) {
		ParserExtResult result = null;
		try {
			result = parseCall0(src, pos, callback);
			if (null != result) {
				return result;
			}
		} catch (Throwable e) {
			throw new TangYuanException(e);
		}
		throw new TangYuanException("命令解析异常[@]:\n" + src);
	}

	private ParserExtResult parseCall0(String src, int pos, JSONExtCallback callback) {
		int leftStart = -1;
		if ((pos + 1 < src.length()) && '{' == src.charAt(pos + 1)) {
			leftStart = pos + 1;
		}
		if (leftStart == -1) {
			return null;
		}
		int endBracketsPos = XmlTextParseUtil.findNestedMatchedChar(src, leftStart, '{', '}');
		if (endBracketsPos == -1) {
			return null;
		}
		String   original = src.substring(leftStart + 1, endBracketsPos).trim();
		Variable variable = new GAParserWarper().parse(original);
		return new ParserExtResult(endBracketsPos, variable.getValue(callback.getArg()));
	}

	private ParserExtResult parse0(String src, int pos, List<SpecialParser> parserList, String flag) {
		ParserExtResult result = null;
		try {
			for (SpecialParser p : parserList) {
				result = p.parse(src, pos);
				if (null != result) {
					return result;
				}
			}
		} catch (Throwable e) {
			throw new TangYuanException(e);
		}
		throw new TangYuanException("命令解析异常[" + flag + "]:\n" + src);
	}

	public static void main(String[] args) {
		//		String fileName = "C:/Users/Lenovo/Desktop/temp/mongo_shell_test.txt";
		//		String str      = readFile(fileName);
		//		System.out.println(str);
		//		JSONExt.parse(str, new JSONExtCallback());

		String           _msDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
		SimpleDateFormat format        = new SimpleDateFormat(_msDateFormat);
		//		SimpleDateFormat format        = new SimpleDateFormat();
		format.setCalendar(new GregorianCalendar(new SimpleTimeZone(8, "GMT")));
		//		o = format.parse(b.get("$date").toString(), new ParsePosition(0));
		System.out.println(format.format(new Date()));

		new Date().toGMTString();
	}

	public static String readFile(String fileName) {
		File            file   = new File(fileName);
		FileInputStream reader = null;
		byte[]          result = null;
		try {
			reader = new FileInputStream(file);
			result = new byte[reader.available()];
			reader.read(result);
		} catch (IOException e) {
		} finally {
			if (null != reader) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		return new String(result, StandardCharsets.UTF_8);
	}

}
