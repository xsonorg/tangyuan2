package com.mongodb.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
		/** 结束的位置 */
		int		endPos;
		Object	value;

		public ParserExtResult(int endPos, Object value) {
			super();
			this.endPos = endPos;
			this.value = value;
		}
	}

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

			return new ParserExtResult(nextEndPos, value);
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
			return new ParserExtResult(nextEndPos, value);
		}
	}

	////////////////////////////////////////////////////////// -->N

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

			String pattern = MongoComponent.getInstance().getDefaultMongoDatePattern();
			Object value = new SimpleDateFormat(pattern, Locale.US).format(new Date());
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

			nextEndPos = XmlTextParseUtil.findNestedMatchedChar(src, nextEndPos, '{', '}');
			if (-1 == nextEndPos) {
				return null;
			}

			String content = src.substring(pos, nextEndPos + 1);
			Code code = new Code(content);

			return new ParserExtResult(nextEndPos, code);
		}
	}

	private List<SpecialParser>	nParserList	= new ArrayList<SpecialParser>();
	private List<SpecialParser>	NParserList	= new ArrayList<SpecialParser>();
	private List<SpecialParser>	IParserList	= new ArrayList<SpecialParser>();
	private List<SpecialParser>	DParserList	= new ArrayList<SpecialParser>();
	private List<SpecialParser>	fParserList	= new ArrayList<SpecialParser>();

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
		String original = src.substring(leftStart + 1, endBracketsPos).trim();
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

}
