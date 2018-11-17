package org.xson.tangyuan.util;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LicensesHelper {

	private static InputStream getResourceAsStream(String resource, ClassLoader classLoader) {
		InputStream returnValue = classLoader.getResourceAsStream(resource);
		if (null == returnValue) {
			returnValue = classLoader.getResourceAsStream("/" + resource);
		}
		if (null != returnValue) {
			return returnValue;
		}
		return null;
	}

	private static char[] load() throws Throwable {
		String resource = "tangyuan-licenses.txt";
		InputStream fis = getResourceAsStream(resource, LicensesHelper.class.getClassLoader());
		byte[] buf = new byte[1024];
		fis.read(buf);
		fis.close();
		return new String(buf).toCharArray();
	}

	private static char[] getContext(InputStream in) throws Throwable {
		byte[] buf = new byte[1024];
		in.read(buf);
		in.close();
		return new String(buf).toCharArray();
	}

	public static boolean check(InputStream in) {
		try {
			char[] a = null;
			if (null == in) {
				a = load();
			} else {
				a = getContext(in);
			}
			char[] d = { '1', '0', '2', '5', '3', '2', '0', '8' };
			d[0] = a[3 * 8 - 1];
			d[1] = a[5 * 8 - 1];
			d[2] = a[7 * 8 - 1];
			d[3] = a[9 * 8 - 1];
			d[4] = a[13 * 8 - 1];
			d[5] = a[16 * 8 - 1];
			d[6] = a[17 * 8 - 1];
			d[7] = a[100 * 8 - 1];
			String t = new SimpleDateFormat("yyyyMMdd").format(new Date());
			return Integer.parseInt(new String(d)) > Integer.parseInt(t);
		} catch (Throwable e) {
		}
		return false;
	}

}
