package org.xson.tangyuan.util;

import java.util.HashMap;
import java.util.Map;

public class MessageFormatter {

	static class FormattingTuple {

		//static public FormattingTuple NULL = new FormattingTuple(null);

		private String    message;
		private Throwable throwable;
		private Object[]  argArray;

		public FormattingTuple(String message) {
			this(message, null, null);
		}

		public FormattingTuple(String message, Object[] argArray, Throwable throwable) {
			this.message = message;
			this.throwable = throwable;
			this.argArray = argArray;
		}

		public String getMessage() {
			return message;
		}

		public Object[] getArgArray() {
			return argArray;
		}

		public Throwable getThrowable() {
			return throwable;
		}
	}

	static final char         DELIM_START = '{';
	static final char         DELIM_STOP  = '}';
	static final String       DELIM_STR   = "{}";
	private static final char ESCAPE_CHAR = '\\';

	// add new
	final public static String formatArgs(final String messagePattern, final Object[] argArray) {
		//		Throwable throwableCandidate = getThrowableCandidate(argArray);
		//		Object[]  args               = argArray;
		//		if (throwableCandidate != null) {
		//			args = trimmedCopy(argArray);
		//		}
		//		return arrayFormat(messagePattern, args, throwableCandidate);
		FormattingTuple tuple = arrayFormat(messagePattern, argArray, null);
		return tuple.getMessage();
	}

	final public static FormattingTuple format(String messagePattern, Object arg) {
		return arrayFormat(messagePattern, new Object[] { arg });
	}

	final public static FormattingTuple format(final String messagePattern, Object arg1, Object arg2) {
		return arrayFormat(messagePattern, new Object[] { arg1, arg2 });
	}

	static final Throwable getThrowableCandidate(Object[] argArray) {
		if (argArray == null || argArray.length == 0) {
			return null;
		}

		final Object lastEntry = argArray[argArray.length - 1];
		if (lastEntry instanceof Throwable) {
			return (Throwable) lastEntry;
		}
		return null;
	}

	final public static FormattingTuple arrayFormat(final String messagePattern, final Object[] argArray) {
		Throwable throwableCandidate = getThrowableCandidate(argArray);
		Object[]  args               = argArray;
		if (throwableCandidate != null) {
			args = trimmedCopy(argArray);
		}
		return arrayFormat(messagePattern, args, throwableCandidate);
	}

	private static Object[] trimmedCopy(Object[] argArray) {
		if (argArray == null || argArray.length == 0) {
			throw new IllegalStateException("non-sensical empty or null argument array");
		}
		final int trimemdLen = argArray.length - 1;
		Object[]  trimmed    = new Object[trimemdLen];
		System.arraycopy(argArray, 0, trimmed, 0, trimemdLen);
		return trimmed;
	}

	final public static FormattingTuple arrayFormat(final String messagePattern, final Object[] argArray, Throwable throwable) {

		if (messagePattern == null) {
			return new FormattingTuple(null, argArray, throwable);
		}

		if (argArray == null) {
			return new FormattingTuple(messagePattern);
		}

		int           i    = 0;
		int           j;
		// use string builder for better multicore performance
		StringBuilder sbuf = new StringBuilder(messagePattern.length() + 50);

		int           L;
		for (L = 0; L < argArray.length; L++) {

			j = messagePattern.indexOf(DELIM_STR, i);

			if (j == -1) {
				// no more variables
				if (i == 0) { // this is a simple string
					return new FormattingTuple(messagePattern, argArray, throwable);
				} else { // add the tail string which contains no variables and return
					// the result.
					sbuf.append(messagePattern, i, messagePattern.length());
					return new FormattingTuple(sbuf.toString(), argArray, throwable);
				}
			} else {
				if (isEscapedDelimeter(messagePattern, j)) {
					if (!isDoubleEscaped(messagePattern, j)) {
						L--; // DELIM_START was escaped, thus should not be incremented
						sbuf.append(messagePattern, i, j - 1);
						sbuf.append(DELIM_START);
						i = j + 1;
					} else {
						// The escape character preceding the delimiter start is
						// itself escaped: "abc x:\\{}"
						// we have to consume one backward slash
						sbuf.append(messagePattern, i, j - 1);
						deeplyAppendParameter(sbuf, argArray[L], new HashMap<Object[], Object>());
						i = j + 2;
					}
				} else {
					// normal case
					sbuf.append(messagePattern, i, j);
					deeplyAppendParameter(sbuf, argArray[L], new HashMap<Object[], Object>());
					i = j + 2;
				}
			}
		}
		// append the characters following the last {} pair.
		sbuf.append(messagePattern, i, messagePattern.length());
		return new FormattingTuple(sbuf.toString(), argArray, throwable);
	}

	final static boolean isEscapedDelimeter(String messagePattern, int delimeterStartIndex) {

		if (delimeterStartIndex == 0) {
			return false;
		}
		char potentialEscape = messagePattern.charAt(delimeterStartIndex - 1);
		if (potentialEscape == ESCAPE_CHAR) {
			return true;
		} else {
			return false;
		}
	}

	final static boolean isDoubleEscaped(String messagePattern, int delimeterStartIndex) {
		if (delimeterStartIndex >= 2 && messagePattern.charAt(delimeterStartIndex - 2) == ESCAPE_CHAR) {
			return true;
		} else {
			return false;
		}
	}

	// special treatment of array values was suggested by 'lizongbo'
	private static void deeplyAppendParameter(StringBuilder sbuf, Object o, Map<Object[], Object> seenMap) {
		if (o == null) {
			sbuf.append("null");
			return;
		}
		if (!o.getClass().isArray()) {
			safeObjectAppend(sbuf, o);
		} else {
			// check for primitive array types because they
			// unfortunately cannot be cast to Object[]
			if (o instanceof boolean[]) {
				booleanArrayAppend(sbuf, (boolean[]) o);
			} else if (o instanceof byte[]) {
				byteArrayAppend(sbuf, (byte[]) o);
			} else if (o instanceof char[]) {
				charArrayAppend(sbuf, (char[]) o);
			} else if (o instanceof short[]) {
				shortArrayAppend(sbuf, (short[]) o);
			} else if (o instanceof int[]) {
				intArrayAppend(sbuf, (int[]) o);
			} else if (o instanceof long[]) {
				longArrayAppend(sbuf, (long[]) o);
			} else if (o instanceof float[]) {
				floatArrayAppend(sbuf, (float[]) o);
			} else if (o instanceof double[]) {
				doubleArrayAppend(sbuf, (double[]) o);
			} else {
				objectArrayAppend(sbuf, (Object[]) o, seenMap);
			}
		}
	}

	private static void safeObjectAppend(StringBuilder sbuf, Object o) {
		try {
			String oAsString = o.toString();
			sbuf.append(oAsString);
		} catch (Throwable t) {
			// Util.report("SLF4J: Failed toString() invocation on an object of type [" + o.getClass().getName() + "]", t);
			sbuf.append("[FAILED toString()]");
		}

	}

	private static void objectArrayAppend(StringBuilder sbuf, Object[] a, Map<Object[], Object> seenMap) {
		sbuf.append('[');
		if (!seenMap.containsKey(a)) {
			seenMap.put(a, null);
			final int len = a.length;
			for (int i = 0; i < len; i++) {
				deeplyAppendParameter(sbuf, a[i], seenMap);
				if (i != len - 1)
					sbuf.append(", ");
			}
			// allow repeats in siblings
			seenMap.remove(a);
		} else {
			sbuf.append("...");
		}
		sbuf.append(']');
	}

	private static void booleanArrayAppend(StringBuilder sbuf, boolean[] a) {
		sbuf.append('[');
		final int len = a.length;
		for (int i = 0; i < len; i++) {
			sbuf.append(a[i]);
			if (i != len - 1)
				sbuf.append(", ");
		}
		sbuf.append(']');
	}

	private static void byteArrayAppend(StringBuilder sbuf, byte[] a) {
		sbuf.append('[');
		final int len = a.length;
		for (int i = 0; i < len; i++) {
			sbuf.append(a[i]);
			if (i != len - 1)
				sbuf.append(", ");
		}
		sbuf.append(']');
	}

	private static void charArrayAppend(StringBuilder sbuf, char[] a) {
		sbuf.append('[');
		final int len = a.length;
		for (int i = 0; i < len; i++) {
			sbuf.append(a[i]);
			if (i != len - 1)
				sbuf.append(", ");
		}
		sbuf.append(']');
	}

	private static void shortArrayAppend(StringBuilder sbuf, short[] a) {
		sbuf.append('[');
		final int len = a.length;
		for (int i = 0; i < len; i++) {
			sbuf.append(a[i]);
			if (i != len - 1)
				sbuf.append(", ");
		}
		sbuf.append(']');
	}

	private static void intArrayAppend(StringBuilder sbuf, int[] a) {
		sbuf.append('[');
		final int len = a.length;
		for (int i = 0; i < len; i++) {
			sbuf.append(a[i]);
			if (i != len - 1)
				sbuf.append(", ");
		}
		sbuf.append(']');
	}

	private static void longArrayAppend(StringBuilder sbuf, long[] a) {
		sbuf.append('[');
		final int len = a.length;
		for (int i = 0; i < len; i++) {
			sbuf.append(a[i]);
			if (i != len - 1)
				sbuf.append(", ");
		}
		sbuf.append(']');
	}

	private static void floatArrayAppend(StringBuilder sbuf, float[] a) {
		sbuf.append('[');
		final int len = a.length;
		for (int i = 0; i < len; i++) {
			sbuf.append(a[i]);
			if (i != len - 1)
				sbuf.append(", ");
		}
		sbuf.append(']');
	}

	private static void doubleArrayAppend(StringBuilder sbuf, double[] a) {
		sbuf.append('[');
		final int len = a.length;
		for (int i = 0; i < len; i++) {
			sbuf.append(a[i]);
			if (i != len - 1)
				sbuf.append(", ");
		}
		sbuf.append(']');
	}
}
