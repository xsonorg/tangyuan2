package org.xson.tangyuan.mongo.util;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.SimpleTimeZone;

import org.bson.types.BSONTimestamp;
import org.bson.types.Decimal128;
import org.xson.tangyuan.mongo.MongoComponent;
import org.xson.tangyuan.xml.method.XmlStaticMethodContainer;

import com.mongodb.util.JSONExtCallback;

/**
 * Mongo 静态工具
 */
public class XmlMongoStaticMethod {

	public static void register() {

		XmlStaticMethodContainer.register("Date", XmlMongoStaticMethod.class.getName() + ".Date");
		XmlStaticMethodContainer.register("Date_D", XmlMongoStaticMethod.class.getName() + ".Date_D");
		XmlStaticMethodContainer.register("Date_DS", XmlMongoStaticMethod.class.getName() + ".Date_DS");

		XmlStaticMethodContainer.register("ISODate", XmlMongoStaticMethod.class.getName() + ".ISODate");
		XmlStaticMethodContainer.register("ISODate_D", XmlMongoStaticMethod.class.getName() + ".ISODate_D");
		XmlStaticMethodContainer.register("ISODate_S", XmlMongoStaticMethod.class.getName() + ".ISODate_S");
		XmlStaticMethodContainer.register("ISODate_SS", XmlMongoStaticMethod.class.getName() + ".ISODate_SS");

		XmlStaticMethodContainer.register("Timestamp", XmlMongoStaticMethod.class.getName() + ".Timestamp");
		XmlStaticMethodContainer.register("Timestamp_I", XmlMongoStaticMethod.class.getName() + ".Timestamp_I");
		XmlStaticMethodContainer.register("Timestamp_L", XmlMongoStaticMethod.class.getName() + ".Timestamp_L");
		XmlStaticMethodContainer.register("Timestamp_D", XmlMongoStaticMethod.class.getName() + ".Timestamp_D");
		XmlStaticMethodContainer.register("Timestamp_S", XmlMongoStaticMethod.class.getName() + ".Timestamp_S");
		XmlStaticMethodContainer.register("Timestamp_SS", XmlMongoStaticMethod.class.getName() + ".Timestamp_SS");

		XmlStaticMethodContainer.register("Decimal128", XmlMongoStaticMethod.class.getName() + ".Decimal128");
	}

	// Date
	public static String Date() {
		return Date_DS(null, null);
	}

	public static String Date_D(java.util.Date date) {
		return Date_DS(date, null);
	}

	public static String Date_DS(java.util.Date date, String pattern) {
		if (null == date) {
			date = new java.util.Date();
		}
		if (null != pattern) {
			return new SimpleDateFormat(pattern, Locale.US).format(date);
		}
		if (date instanceof java.sql.Time) {
			return new SimpleDateFormat(JSONExtCallback._timeFormat, Locale.US).format(date);
		}
		if (date instanceof java.sql.Date) {
			return new SimpleDateFormat(JSONExtCallback._dateFormat, Locale.US).format(date);
		}
		pattern = MongoComponent.getInstance().getDefaultMongoDatePattern();
		return new SimpleDateFormat(pattern, Locale.US).format(date);
	}

	// ISODate
	public static java.util.Date ISODate() {
		return ISODate_D(null);
	}

	public static java.util.Date ISODate_D(java.util.Date date) {
		if (null == date) {
			return new java.util.Date();
		}
		return date;
	}

	public static java.util.Date ISODate_S(String dateString) {
		return ISODate_SS(dateString, null);
	}

	public static java.util.Date ISODate_SS(String dateString, String pattern) {
		if (null != pattern) {
			Date             isoDate = null;
			SimpleDateFormat format  = new SimpleDateFormat(pattern);
			format.setCalendar(new GregorianCalendar(new SimpleTimeZone(0, "GMT")));
			isoDate = format.parse(dateString, new ParsePosition(0));
			return isoDate;
		}
		return JSONExtCallback.parseISODate(dateString);
	}

	// Timestamp
	public static BSONTimestamp Timestamp() {
		return new BSONTimestamp();
	}

	public static BSONTimestamp Timestamp_I(int time) {
		return new BSONTimestamp(time, 1);
	}

	public static BSONTimestamp Timestamp_L(long time) {
		return new BSONTimestamp((int) (time / 1000), 1);
	}

	public static BSONTimestamp Timestamp_D(java.util.Date date) {
		return Timestamp_L(date.getTime());
	}

	public static BSONTimestamp Timestamp_S(String dateString) {
		return Timestamp_SS(dateString, null);
	}

	public static BSONTimestamp Timestamp_SS(String dateString, String pattern) {
		Date isoDate = ISODate_SS(dateString, pattern);
		return Timestamp_D(isoDate);
	}

	// Decimal128
	public static Decimal128 Decimal128(Object val) {
		return Decimal128.parse(val.toString());
	}

}
