package org.xson.tangyuan.util;

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

	// public static final String DATE_TIME_FORMAT_SHORT = "yyyyMMddHHmmss";
	// public static final String DATE_TIME_FORMAT_YMD = "yyyy/MM/dd HH:mm:ss";
	// public static final String DATE_TIME_FORMAT_NORMAL = "yyyy-MM-dd HH:mm:ss";
	// public static final String DATE_TIME_FORMAT_ENGLISH = "MM/dd/yyyy HH:mm:ss";
	// public static final String DATE_TIME_FORMAT_MINUTE = "yyyyMMddHHmm";
	//
	// public static final String TIME_FORMAT_SHORT_NORMAL = "HH:mm:ss";
	// public static final String TIME_FORMAT_SHORT_SHORT = "HHmmss";
	//
	// public static final String DATE_FORMAT_SHORT = "yyyyMMdd";
	// public static final String DATE_FORMAT_NORMAL = "yyyy-MM-dd";
	// public static final String DATE_FORMAT_ENGLISH = "MM/dd/yyyy";
	// public static final String DATE_FORMAT_MONTH = "yyyyMM";
	// public static final String DATE_FORMAT_YEAR_MONTH = "yyyy-MM";
	// public static final String DATE_FORMAT_MONTH_DAY_FORMAT = "MM-dd";

	public static final String	TIME_FORMAT_SHORT				= "yyyyMMddHHmmss";
	public static final String	TIME_FORMAT_YMD					= "yyyy/MM/dd HH:mm:ss";
	public static final String	TIME_FORMAT_NORMAL				= "yyyy-MM-dd HH:mm:ss";
	public static final String	TIME_FORMAT_ENGLISH				= "MM/dd/yyyy HH:mm:ss";
	public static final String	TIME_FORMAT_CHINA				= "yyyy年MM月dd日 HH时mm分ss秒";
	public static final String	TIME_FORMAT_CHINA_S				= "yyyy年M月d日 H时m分s秒";
	public static final String	TIME_FORMAT_SHORT_S				= "HH:mm:ss";

	public static final String	DATE_FORMAT_SHORT				= "yyyyMMdd";
	public static final String	DATE_FORMAT_NORMAL				= "yyyy-MM-dd";
	public static final String	DATE_FORMAT_ENGLISH				= "MM/dd/yyyy";
	public static final String	DATE_FORMAT_CHINA				= "yyyy年MM月dd日";
	public static final String	DATE_FORMAT_CHINA_YEAR_MONTH	= "yyyy年MM月";
	public static final String	MONTH_FORMAT					= "yyyyMM";
	public static final String	YEAR_MONTH_FORMAT				= "yyyy-MM";
	public static final String	DATE_FORMAT_MINUTE				= "yyyyMMddHHmm";
	public static final String	MONTH_DAY_FORMAT				= "MM-dd";

	public static boolean isTime(String var) {
		if (isDateTime(var)) {
			return true;
		} else if (isOnlyDate(var)) {
			return true;
		} else if (isDateTime(var)) {
			return true;
		}
		return false;
	}

	public static boolean isOnlyDate(String var) {
		if (var.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) {
			return true;
		}
		return false;
	}

	public static boolean isOnlyTime(String var) {
		if (var.matches("\\d{1,2}:\\d{1,2}:\\d{1,2}")) {
			return true;
		}
		return false;
	}

	public static boolean isDateTime(String var) {
		if (var.matches("\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}")) {
			return true;
		}
		return false;
	}

	public static Date parseDate(String var) {
		DateFormat fmt = null;
		if (var.matches("\\d{14}")) {
			fmt = new SimpleDateFormat(TIME_FORMAT_SHORT);
		} else if (var.matches("\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}")) {
			fmt = new SimpleDateFormat(TIME_FORMAT_NORMAL);
		} else if (var.matches("\\d{1,2}/\\d{1,2}/\\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2}")) {
			fmt = new SimpleDateFormat(TIME_FORMAT_ENGLISH);
		} else if (var.matches("\\d{4}年\\d{1,2}月\\d{1,2}日 \\d{1,2}时\\d{1,2}分\\d{1,2}秒")) {
			fmt = new SimpleDateFormat(TIME_FORMAT_CHINA);
		} else if (var.matches("\\d{8}")) {
			fmt = new SimpleDateFormat(DATE_FORMAT_SHORT);
		} else if (var.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) {
			fmt = new SimpleDateFormat(DATE_FORMAT_NORMAL);
		} else if (var.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
			fmt = new SimpleDateFormat(DATE_FORMAT_ENGLISH);
		} else if (var.matches("\\d{4}年\\d{1,2}月\\d{1,2}日")) {
			fmt = new SimpleDateFormat(DATE_FORMAT_CHINA);
		} else if (var.matches("\\d{4}\\d{1,2}\\d{1,2}\\d{1,2}\\d{1,2}")) {
			fmt = new SimpleDateFormat(DATE_FORMAT_MINUTE);
		} else if (var.matches("\\d{1,2}:\\d{1,2}:\\d{1,2}")) {
			fmt = new SimpleDateFormat(TIME_FORMAT_SHORT_S);
		}
		try {
			return fmt.parse(var);
		} catch (ParseException e) {
			throw new IllegalArgumentException("Date or Time String is invalid.");
		}
	}

	public static java.sql.Date parseSqlDate(String var) {
		Date date = parseDate(var);
		return new java.sql.Date(date.getTime());
	}

	public static Time parseSqlTime(String var) {
		Date date = parseDate(var);
		return new Time(date.getTime());
	}

	public static String getDateTimeString(java.util.Date date) {
		return getDateTimeString(date, TIME_FORMAT_NORMAL);
	}

	public static String getDateTimeString(java.util.Date date, String pattern) {
		DateFormat fmt = new SimpleDateFormat(pattern);
		return fmt.format(date);
	}

	/////

	public static String getDateString(java.sql.Date date) {
		return getDateString(date, DATE_FORMAT_NORMAL);
	}

	public static String getDateString(java.sql.Date date, String pattern) {
		DateFormat fmt = new SimpleDateFormat(pattern);
		return fmt.format(date);
	}

	public static String getTimeString(java.sql.Time date) {
		return getTimeString(date, TIME_FORMAT_SHORT_S);
	}

	public static String getTimeString(java.sql.Time date, String pattern) {
		DateFormat fmt = new SimpleDateFormat(pattern);
		return fmt.format(date);
	}

	public static String getTimestampString(java.sql.Timestamp date) {
		return getTimestampString(date, DATE_FORMAT_NORMAL);
	}

	public static String getTimestampString(java.sql.Timestamp date, String pattern) {
		DateFormat fmt = new SimpleDateFormat(pattern);
		return fmt.format(date);
	}
}
