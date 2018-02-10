package org.xson.tangyuan.share.util;

public class TangYuanUtil {

	public static String format(String str, Object... args) {
		if (null == args || 0 == args.length) {
			return str;
		}
		for (int i = 0; i < args.length; i++) {
			str = str.replaceFirst("\\{\\}", String.valueOf(args[i]));
		}
		return str;
	}

}
