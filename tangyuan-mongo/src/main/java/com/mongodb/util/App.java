package com.mongodb.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class App {

	public static void main(String[] args) {

		//		"Wed Nov 04 2020 15:55:30 GMT+0800 (CST)"
		//		"Wed Nov 04 15:57:55 CST 2020"

		SimpleDateFormat format1 = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss z X Z", Locale.US);
		//SimpleDateFormat format2 = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss z X Z", Locale.US);
		SimpleDateFormat format2 = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss z X Z", Locale.US);
		format2.setCalendar(new GregorianCalendar(new SimpleTimeZone(8, "GMT")));
		SimpleDateFormat format3 = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss zZ", Locale.US);
		//		format3.setCalendar(new GregorianCalendar(new SimpleTimeZone(0, "GMT+08:00")));

		System.out.println(format1.format(new Date()));
		System.out.println(format2.format(new Date()));
		System.out.println(format3.format(new Date()));

		SimpleDateFormat format4 = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss zZ", Locale.ENGLISH);
		//		format4.setCalendar(new GregorianCalendar(new SimpleTimeZone(8, "GMT")));
		// format4.setTimeZone(new SimpleTimeZone(8, "GMT"));
		//		format4.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
		format4.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		// format4.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
		//		format4.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

		// For example, TimeZone.getTimeZone("GMT-8").getID() returns "GMT-08:00". 

		Date x = new Date();
		format4.format(x);

		System.out.println(format4.format(new Date()));

		//		new Date().toLocaleString();
		//		new Date().toGMTString();
		//		System.out.println(new Date().getTimezoneOffset());

		//		System.out.println(new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss zZ", Locale.ENGLISH).format(new Date()));

		//		System.out.println(new Date().toString());
		TimeZone tz = TimeZone.getDefault();
		System.out.println(tz.getDisplayName());
		System.out.println(tz.getDisplayName(Locale.ENGLISH));
		System.out.println(tz.getID());

		TimeZone tz1 = TimeZone.getTimeZone("CST");
		System.out.println(tz1.getDisplayName());
		System.out.println(tz1.getDisplayName(Locale.ENGLISH));
		System.out.println(tz1.getID());

		System.out.println(new Date().toString());

		System.out.println(new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss zZ").format(new Date()));
		System.out.println(new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss zZ", Locale.US).format(new Date()));
	}
}
