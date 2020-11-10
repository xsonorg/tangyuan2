package org.xson.tangyuan.util;

public class HashUtil {

	public static int jdkHashCode(String str) {
		int h = 0;
		if (null == str) {
			return h;
		}
		int l = str.length();
		for (int i = 0; i < l; i++) {
			h = 31 * h + str.charAt(i);
		}
		return h;
	}

	//	public static void main(String[] args) {
	//		System.out.println("xabc".hashCode());
	//		System.out.println(jdkHashCode("abc"));
	//	}
}
