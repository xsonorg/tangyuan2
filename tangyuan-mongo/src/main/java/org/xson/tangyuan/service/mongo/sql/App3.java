package org.xson.tangyuan.service.mongo.sql;

public class App3 {

	protected String[] splitSetItem(String src, char separator) {
		int     splitPos = -1;
		boolean isString = false;
		for (int i = 0; i < src.length(); i++) {
			char key = src.charAt(i);
			switch (key) {
			case '\'':
				isString = !isString;
				break;
			default:
				if (separator == key && !isString) {
					splitPos = i;
				}
				break;
			}
			if (splitPos > -1) {
				break;
			}
		}
		if (-1 == splitPos) {
			return null;
		}

		String[] array = new String[2];
		array[0] = src.substring(0, splitPos);
		array[1] = src.substring(splitPos + 1);

		System.out.println(array[0]);
		System.out.println(array[1]);
		return array;
	}

	public static void main(String[] args) {
		//		App3   x   = new App3();
		//		// a=1, b=[a: 1, b: 3], d=128, e={a: 18} 
		//		String str = "b =  {a: 18} ";
		//		x.splitSetItem(str, '=');
		//		
		//		DBObject query  = new BasicDBObject();
		//		System.out.println(query);

		System.out.println(System.currentTimeMillis());
	}
}
