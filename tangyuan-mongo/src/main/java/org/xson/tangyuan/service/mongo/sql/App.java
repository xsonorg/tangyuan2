package org.xson.tangyuan.service.mongo.sql;

public class App {

	public final static String	BLANK_MARK		= " ";
	public final static String	SELECT_MARK		= "SELECT";
	public final static String	INSERT_MARK		= "INSERT INTO";
	public final static String	UPDATE_MARK		= "UPDATE";
	public final static String	DELETE_MARK		= "DELETE FROM";

	public final static String	FROM_MARK		= " FROM ";
	public final static String	WHERE_MARK		= " WHERE ";
	public final static String	ORDER_BY_MARK	= " ORDER BY ";
	public final static String	LIMIT_MARK		= " LIMIT ";

	public static void main(String[] args) {
		//String ucSql = "UPDATE USER SET USER_ID = 111, USER_NAME = '李四111' 	WHERE _ID = '583830752C591117AC94412D'";
		String ucSql = "1' 	WHERE _ID = '583";
		//System.out.println();
		
		// int wherePos = ucSql.indexOf(WHERE_MARK, 12);
		int wherePos = ucSql.indexOf(" WHERE", 12);

		for (int i = 0; i < ucSql.length(); i++) {
			char x = ucSql.charAt(i);
			int y = x;
			System.out.println(x + ":" + Integer.toHexString(y));
		}
		
		System.out.println(wherePos);
	}
}
