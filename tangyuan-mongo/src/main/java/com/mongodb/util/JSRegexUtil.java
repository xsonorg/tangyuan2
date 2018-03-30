package com.mongodb.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class JSRegexUtil {

	private static final int						FLAG_GLOBAL	= 256;

	// private static final int[] FLAG_LOOKUP = new int[Character.MAX_VALUE];
	private static final Map<Character, Integer>	FLAG_LOOKUP	= new HashMap<Character, Integer>();

	static {
		FLAG_LOOKUP.put('g', FLAG_GLOBAL);
		FLAG_LOOKUP.put('i', Pattern.CASE_INSENSITIVE);
		FLAG_LOOKUP.put('m', Pattern.MULTILINE);
		FLAG_LOOKUP.put('s', Pattern.DOTALL);
		FLAG_LOOKUP.put('c', Pattern.CANON_EQ);
		FLAG_LOOKUP.put('x', Pattern.COMMENTS);
		FLAG_LOOKUP.put('d', Pattern.UNIX_LINES);
		FLAG_LOOKUP.put('t', Pattern.LITERAL);
		FLAG_LOOKUP.put('u', Pattern.UNICODE_CASE);
	}

	public static boolean isFlag(char chr) {
		return FLAG_LOOKUP.containsKey(chr);
	}

	public static int regexFlags(char chr, int flags) {
		flags |= FLAG_LOOKUP.get(chr).intValue();
		return flags;
	}
}
