package org.xson.tangyuan.ognl.vars.vo;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.xson.tangyuan.ognl.OgnlException;

/**
 * 逻辑表达式运算
 */
public class LogicalExpr {

	public static int	scale			= 4;
	public static int	roundingMode	= BigDecimal.ROUND_HALF_DOWN;

	/**
	 * 相等
	 */
	public static boolean numberEqual(Number x, Number y) {

		if (x instanceof Integer) {
			if (y instanceof Integer) {
				return ((Integer) x).intValue() == ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Integer) x).intValue() == ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Integer) x).intValue() == ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Integer) x).intValue() == ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Integer) x).toString()).compareTo((BigInteger) y) == 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Integer) x).toString()).compareTo((BigDecimal) y) == 0;
			} else if (y instanceof Short) {
				return ((Integer) x).intValue() == ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Integer) x).intValue() == ((Byte) y).byteValue();
			}
		} else if (x instanceof Long) {
			if (y instanceof Integer) {
				return ((Long) x).longValue() == ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Long) x).longValue() == ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Long) x).longValue() == ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Long) x).longValue() == ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Long) x).toString()).compareTo((BigInteger) y) == 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Long) x).toString()).compareTo((BigDecimal) y) == 0;
			} else if (y instanceof Short) {
				return ((Long) x).longValue() == ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Long) x).longValue() == ((Byte) y).byteValue();
			}
		} else if (x instanceof Float) {
			if (y instanceof Integer) {
				return ((Float) x).floatValue() == ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Float) x).floatValue() == ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Float) x).floatValue() == ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Float) x).floatValue() == ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigDecimal(((Float) x).toString()).compareTo(new BigDecimal((BigInteger) y)) == 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Float) x).toString()).compareTo((BigDecimal) y) == 0;
			} else if (y instanceof Short) {
				return ((Float) x).floatValue() == ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Float) x).floatValue() == ((Byte) y).byteValue();
			}
		} else if (x instanceof Double) {
			if (y instanceof Integer) {
				return ((Double) x).doubleValue() == ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Double) x).doubleValue() == ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Double) x).doubleValue() == ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Double) x).doubleValue() == ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigDecimal(((Double) x).toString()).compareTo(new BigDecimal((BigInteger) y)) == 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Double) x).toString()).compareTo((BigDecimal) y) == 0;
			} else if (y instanceof Short) {
				return ((Double) x).doubleValue() == ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Double) x).doubleValue() == ((Byte) y).byteValue();
			}
		} else if (x instanceof BigInteger) {
			if (y instanceof Integer) {
				return ((BigInteger) x).compareTo(new BigInteger(((Integer) y).toString())) == 0;
			} else if (y instanceof Long) {
				return ((BigInteger) x).compareTo(new BigInteger(((Long) y).toString())) == 0;
			} else if (y instanceof Float) {
				return (new BigDecimal((BigInteger) x)).compareTo(new BigDecimal(((Float) y).toString())) == 0;
			} else if (y instanceof Double) {
				return (new BigDecimal((BigInteger) x)).compareTo(new BigDecimal(((Double) y).toString())) == 0;
			} else if (y instanceof BigInteger) {
				return ((BigInteger) x).compareTo((BigInteger) y) == 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal((BigInteger) x).compareTo((BigDecimal) y) == 0;
			} else if (y instanceof Short) {
				return ((BigInteger) x).compareTo(new BigInteger(((Short) y).toString())) == 0;
			} else if (y instanceof Byte) {
				return ((BigInteger) x).compareTo(new BigInteger(((Byte) y).toString())) == 0;
			}
		} else if (x instanceof BigDecimal) {
			if (y instanceof Integer) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Integer) y).toString())) == 0;
			} else if (y instanceof Long) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Long) y).toString())) == 0;
			} else if (y instanceof Float) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Float) y).toString())) == 0;
			} else if (y instanceof Double) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Double) y).toString())) == 0;
			} else if (y instanceof BigInteger) {
				return ((BigDecimal) x).compareTo(new BigDecimal((BigInteger) y)) == 0;
			} else if (y instanceof BigDecimal) {
				return ((BigDecimal) x).compareTo((BigDecimal) y) == 0;
			} else if (y instanceof Short) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Short) y).toString())) == 0;
			} else if (y instanceof Byte) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Byte) y).toString())) == 0;
			}
		} else if (x instanceof Short) {
			if (y instanceof Integer) {
				return ((Short) x).shortValue() == ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Short) x).shortValue() == ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Short) x).shortValue() == ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Short) x).shortValue() == ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Short) x).toString()).compareTo((BigInteger) y) == 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Short) x).toString()).compareTo((BigDecimal) y) == 0;
			} else if (y instanceof Short) {
				return ((Short) x).shortValue() == ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Short) x).shortValue() == ((Byte) y).byteValue();
			}
		} else if (x instanceof Byte) {
			if (y instanceof Integer) {
				return ((Byte) x).byteValue() == ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Byte) x).byteValue() == ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Byte) x).byteValue() == ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Byte) x).byteValue() == ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Byte) x).toString()).compareTo((BigInteger) y) == 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Byte) x).toString()).compareTo((BigDecimal) y) == 0;
			} else if (y instanceof Short) {
				return ((Byte) x).byteValue() == ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Byte) x).byteValue() == ((Byte) y).byteValue();
			}
		}
		throw new OgnlException("Unsupported logical expression(==) object. x:" + x.getClass().getName() + ", y:" + y.getClass().getName());
	}

	/**
	 * 不相等
	 */
	public static boolean numberNotEqual(Number x, Number y) {
		if (x instanceof Integer) {
			if (y instanceof Integer) {
				return ((Integer) x).intValue() != ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Integer) x).intValue() != ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Integer) x).intValue() != ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Integer) x).intValue() != ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Integer) x).toString()).compareTo((BigInteger) y) != 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Integer) x).toString()).compareTo((BigDecimal) y) != 0;
			} else if (y instanceof Short) {
				return ((Integer) x).intValue() != ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Integer) x).intValue() != ((Byte) y).byteValue();
			}
		} else if (x instanceof Long) {
			if (y instanceof Integer) {
				return ((Long) x).longValue() != ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Long) x).longValue() != ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Long) x).longValue() != ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Long) x).longValue() != ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Long) x).toString()).compareTo((BigInteger) y) != 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Long) x).toString()).compareTo((BigDecimal) y) != 0;
			} else if (y instanceof Short) {
				return ((Long) x).longValue() != ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Long) x).longValue() != ((Byte) y).byteValue();
			}
		} else if (x instanceof Float) {
			if (y instanceof Integer) {
				return ((Float) x).floatValue() != ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Float) x).floatValue() != ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Float) x).floatValue() != ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Float) x).floatValue() != ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigDecimal(((Float) x).toString()).compareTo(new BigDecimal((BigInteger) y)) != 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Float) x).toString()).compareTo((BigDecimal) y) != 0;
			} else if (y instanceof Short) {
				return ((Float) x).floatValue() != ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Float) x).floatValue() != ((Byte) y).byteValue();
			}
		} else if (x instanceof Double) {
			if (y instanceof Integer) {
				return ((Double) x).doubleValue() != ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Double) x).doubleValue() != ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Double) x).doubleValue() != ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Double) x).doubleValue() != ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigDecimal(((Double) x).toString()).compareTo(new BigDecimal((BigInteger) y)) != 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Double) x).toString()).compareTo((BigDecimal) y) != 0;
			} else if (y instanceof Short) {
				return ((Double) x).doubleValue() != ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Double) x).doubleValue() != ((Byte) y).byteValue();
			}
		} else if (x instanceof BigInteger) {
			if (y instanceof Integer) {
				return ((BigInteger) x).compareTo(new BigInteger(((Integer) y).toString())) != 0;
			} else if (y instanceof Long) {
				return ((BigInteger) x).compareTo(new BigInteger(((Long) y).toString())) != 0;
			} else if (y instanceof Float) {
				return (new BigDecimal((BigInteger) x)).compareTo(new BigDecimal(((Float) y).toString())) != 0;
			} else if (y instanceof Double) {
				return (new BigDecimal((BigInteger) x)).compareTo(new BigDecimal(((Double) y).toString())) != 0;
			} else if (y instanceof BigInteger) {
				return ((BigInteger) x).compareTo((BigInteger) y) != 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal((BigInteger) x).compareTo((BigDecimal) y) != 0;
			} else if (y instanceof Short) {
				return ((BigInteger) x).compareTo(new BigInteger(((Short) y).toString())) != 0;
			} else if (y instanceof Byte) {
				return ((BigInteger) x).compareTo(new BigInteger(((Byte) y).toString())) != 0;
			}
		} else if (x instanceof BigDecimal) {
			if (y instanceof Integer) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Integer) y).toString())) != 0;
			} else if (y instanceof Long) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Long) y).toString())) != 0;
			} else if (y instanceof Float) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Float) y).toString())) != 0;
			} else if (y instanceof Double) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Double) y).toString())) != 0;
			} else if (y instanceof BigInteger) {
				return ((BigDecimal) x).compareTo(new BigDecimal((BigInteger) y)) != 0;
			} else if (y instanceof BigDecimal) {
				return ((BigDecimal) x).compareTo((BigDecimal) y) != 0;
			} else if (y instanceof Short) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Short) y).toString())) != 0;
			} else if (y instanceof Byte) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Byte) y).toString())) != 0;
			}
		} else if (x instanceof Short) {
			if (y instanceof Integer) {
				return ((Short) x).shortValue() != ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Short) x).shortValue() != ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Short) x).shortValue() != ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Short) x).shortValue() != ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Short) x).toString()).compareTo((BigInteger) y) != 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Short) x).toString()).compareTo((BigDecimal) y) != 0;
			} else if (y instanceof Short) {
				return ((Short) x).shortValue() != ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Short) x).shortValue() != ((Byte) y).byteValue();
			}
		} else if (x instanceof Byte) {
			if (y instanceof Integer) {
				return ((Byte) x).byteValue() != ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Byte) x).byteValue() != ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Byte) x).byteValue() != ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Byte) x).byteValue() != ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Byte) x).toString()).compareTo((BigInteger) y) != 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Byte) x).toString()).compareTo((BigDecimal) y) != 0;
			} else if (y instanceof Short) {
				return ((Byte) x).byteValue() != ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Byte) x).byteValue() != ((Byte) y).byteValue();
			}
		}
		throw new OgnlException("Unsupported logical expression(!=) object. x:" + x.getClass().getName() + ", y:" + y.getClass().getName());
	}

	/**
	 * 大于
	 */
	public static boolean numberMoreThan(Number x, Number y) {
		if (x instanceof Integer) {
			if (y instanceof Integer) {
				return ((Integer) x).intValue() > ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Integer) x).intValue() > ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Integer) x).intValue() > ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Integer) x).intValue() > ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Integer) x).toString()).compareTo((BigInteger) y) > 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Integer) x).toString()).compareTo((BigDecimal) y) > 0;
			} else if (y instanceof Short) {
				return ((Integer) x).intValue() > ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Integer) x).intValue() > ((Byte) y).byteValue();
			}
		} else if (x instanceof Long) {
			if (y instanceof Integer) {
				return ((Long) x).longValue() > ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Long) x).longValue() > ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Long) x).longValue() > ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Long) x).longValue() > ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Long) x).toString()).compareTo((BigInteger) y) > 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Long) x).toString()).compareTo((BigDecimal) y) > 0;
			} else if (y instanceof Short) {
				return ((Long) x).longValue() > ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Long) x).longValue() > ((Byte) y).byteValue();
			}
		} else if (x instanceof Float) {
			if (y instanceof Integer) {
				return ((Float) x).floatValue() > ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Float) x).floatValue() > ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Float) x).floatValue() > ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Float) x).floatValue() > ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigDecimal(((Float) x).toString()).compareTo(new BigDecimal((BigInteger) y)) > 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Float) x).toString()).compareTo((BigDecimal) y) > 0;
			} else if (y instanceof Short) {
				return ((Float) x).floatValue() > ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Float) x).floatValue() > ((Byte) y).byteValue();
			}
		} else if (x instanceof Double) {
			if (y instanceof Integer) {
				return ((Double) x).doubleValue() > ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Double) x).doubleValue() > ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Double) x).doubleValue() > ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Double) x).doubleValue() > ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigDecimal(((Double) x).toString()).compareTo(new BigDecimal((BigInteger) y)) > 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Double) x).toString()).compareTo((BigDecimal) y) > 0;
			} else if (y instanceof Short) {
				return ((Double) x).doubleValue() > ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Double) x).doubleValue() > ((Byte) y).byteValue();
			}
		} else if (x instanceof BigInteger) {
			if (y instanceof Integer) {
				return ((BigInteger) x).compareTo(new BigInteger(((Integer) y).toString())) > 0;
			} else if (y instanceof Long) {
				return ((BigInteger) x).compareTo(new BigInteger(((Long) y).toString())) > 0;
			} else if (y instanceof Float) {
				return (new BigDecimal((BigInteger) x)).compareTo(new BigDecimal(((Float) y).toString())) > 0;
			} else if (y instanceof Double) {
				return (new BigDecimal((BigInteger) x)).compareTo(new BigDecimal(((Double) y).toString())) > 0;
			} else if (y instanceof BigInteger) {
				return ((BigInteger) x).compareTo((BigInteger) y) > 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal((BigInteger) x).compareTo((BigDecimal) y) > 0;
			} else if (y instanceof Short) {
				return ((BigInteger) x).compareTo(new BigInteger(((Short) y).toString())) > 0;
			} else if (y instanceof Byte) {
				return ((BigInteger) x).compareTo(new BigInteger(((Byte) y).toString())) > 0;
			}
		} else if (x instanceof BigDecimal) {
			if (y instanceof Integer) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Integer) y).toString())) > 0;
			} else if (y instanceof Long) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Long) y).toString())) > 0;
			} else if (y instanceof Float) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Float) y).toString())) > 0;
			} else if (y instanceof Double) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Double) y).toString())) > 0;
			} else if (y instanceof BigInteger) {
				return ((BigDecimal) x).compareTo(new BigDecimal((BigInteger) y)) > 0;
			} else if (y instanceof BigDecimal) {
				return ((BigDecimal) x).compareTo((BigDecimal) y) > 0;
			} else if (y instanceof Short) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Short) y).toString())) > 0;
			} else if (y instanceof Byte) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Byte) y).toString())) > 0;
			}
		} else if (x instanceof Short) {
			if (y instanceof Integer) {
				return ((Short) x).shortValue() > ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Short) x).shortValue() > ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Short) x).shortValue() > ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Short) x).shortValue() > ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Short) x).toString()).compareTo((BigInteger) y) > 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Short) x).toString()).compareTo((BigDecimal) y) > 0;
			} else if (y instanceof Short) {
				return ((Short) x).shortValue() > ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Short) x).shortValue() > ((Byte) y).byteValue();
			}
		} else if (x instanceof Byte) {
			if (y instanceof Integer) {
				return ((Byte) x).byteValue() > ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Byte) x).byteValue() > ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Byte) x).byteValue() > ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Byte) x).byteValue() > ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Byte) x).toString()).compareTo((BigInteger) y) > 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Byte) x).toString()).compareTo((BigDecimal) y) > 0;
			} else if (y instanceof Short) {
				return ((Byte) x).byteValue() > ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Byte) x).byteValue() > ((Byte) y).byteValue();
			}
		}
		throw new OgnlException("Unsupported logical expression(>) object. x:" + x.getClass().getName() + ", y:" + y.getClass().getName());
	}

	/**
	 * 大于等于
	 */
	public static boolean numberMoreThanEqual(Number x, Number y) {
		if (x instanceof Integer) {
			if (y instanceof Integer) {
				return ((Integer) x).intValue() >= ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Integer) x).intValue() >= ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Integer) x).intValue() >= ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Integer) x).intValue() >= ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Integer) x).toString()).compareTo((BigInteger) y) >= 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Integer) x).toString()).compareTo((BigDecimal) y) >= 0;
			} else if (y instanceof Short) {
				return ((Integer) x).intValue() >= ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Integer) x).intValue() >= ((Byte) y).byteValue();
			}
		} else if (x instanceof Long) {
			if (y instanceof Integer) {
				return ((Long) x).longValue() >= ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Long) x).longValue() >= ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Long) x).longValue() >= ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Long) x).longValue() >= ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Long) x).toString()).compareTo((BigInteger) y) >= 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Long) x).toString()).compareTo((BigDecimal) y) >= 0;
			} else if (y instanceof Short) {
				return ((Long) x).longValue() >= ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Long) x).longValue() >= ((Byte) y).byteValue();
			}
		} else if (x instanceof Float) {
			if (y instanceof Integer) {
				return ((Float) x).floatValue() >= ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Float) x).floatValue() >= ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Float) x).floatValue() >= ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Float) x).floatValue() >= ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigDecimal(((Float) x).toString()).compareTo(new BigDecimal((BigInteger) y)) >= 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Float) x).toString()).compareTo((BigDecimal) y) >= 0;
			} else if (y instanceof Short) {
				return ((Float) x).floatValue() >= ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Float) x).floatValue() >= ((Byte) y).byteValue();
			}
		} else if (x instanceof Double) {
			if (y instanceof Integer) {
				return ((Double) x).doubleValue() >= ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Double) x).doubleValue() >= ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Double) x).doubleValue() >= ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Double) x).doubleValue() >= ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigDecimal(((Double) x).toString()).compareTo(new BigDecimal((BigInteger) y)) >= 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Double) x).toString()).compareTo((BigDecimal) y) >= 0;
			} else if (y instanceof Short) {
				return ((Double) x).doubleValue() >= ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Double) x).doubleValue() >= ((Byte) y).byteValue();
			}
		} else if (x instanceof BigInteger) {
			if (y instanceof Integer) {
				return ((BigInteger) x).compareTo(new BigInteger(((Integer) y).toString())) >= 0;
			} else if (y instanceof Long) {
				return ((BigInteger) x).compareTo(new BigInteger(((Long) y).toString())) >= 0;
			} else if (y instanceof Float) {
				return (new BigDecimal((BigInteger) x)).compareTo(new BigDecimal(((Float) y).toString())) >= 0;
			} else if (y instanceof Double) {
				return (new BigDecimal((BigInteger) x)).compareTo(new BigDecimal(((Double) y).toString())) >= 0;
			} else if (y instanceof BigInteger) {
				return ((BigInteger) x).compareTo((BigInteger) y) >= 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal((BigInteger) x).compareTo((BigDecimal) y) >= 0;
			} else if (y instanceof Short) {
				return ((BigInteger) x).compareTo(new BigInteger(((Short) y).toString())) >= 0;
			} else if (y instanceof Byte) {
				return ((BigInteger) x).compareTo(new BigInteger(((Byte) y).toString())) >= 0;
			}
		} else if (x instanceof BigDecimal) {
			if (y instanceof Integer) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Integer) y).toString())) >= 0;
			} else if (y instanceof Long) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Long) y).toString())) >= 0;
			} else if (y instanceof Float) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Float) y).toString())) >= 0;
			} else if (y instanceof Double) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Double) y).toString())) >= 0;
			} else if (y instanceof BigInteger) {
				return ((BigDecimal) x).compareTo(new BigDecimal((BigInteger) y)) >= 0;
			} else if (y instanceof BigDecimal) {
				return ((BigDecimal) x).compareTo((BigDecimal) y) >= 0;
			} else if (y instanceof Short) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Short) y).toString())) >= 0;
			} else if (y instanceof Byte) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Byte) y).toString())) >= 0;
			}
		} else if (x instanceof Short) {
			if (y instanceof Integer) {
				return ((Short) x).shortValue() >= ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Short) x).shortValue() >= ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Short) x).shortValue() >= ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Short) x).shortValue() >= ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Short) x).toString()).compareTo((BigInteger) y) >= 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Short) x).toString()).compareTo((BigDecimal) y) >= 0;
			} else if (y instanceof Short) {
				return ((Short) x).shortValue() >= ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Short) x).shortValue() >= ((Byte) y).byteValue();
			}
		} else if (x instanceof Byte) {
			if (y instanceof Integer) {
				return ((Byte) x).byteValue() >= ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Byte) x).byteValue() >= ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Byte) x).byteValue() >= ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Byte) x).byteValue() >= ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Byte) x).toString()).compareTo((BigInteger) y) >= 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Byte) x).toString()).compareTo((BigDecimal) y) >= 0;
			} else if (y instanceof Short) {
				return ((Byte) x).byteValue() >= ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Byte) x).byteValue() >= ((Byte) y).byteValue();
			}
		}
		throw new OgnlException("Unsupported logical expression(>=) object. x:" + x.getClass().getName() + ", y:" + y.getClass().getName());
	}

	/**
	 * 小于
	 */
	public static boolean numberLessThan(Number x, Number y) {
		if (x instanceof Integer) {
			if (y instanceof Integer) {
				return ((Integer) x).intValue() < ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Integer) x).intValue() < ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Integer) x).intValue() < ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Integer) x).intValue() < ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Integer) x).toString()).compareTo((BigInteger) y) < 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Integer) x).toString()).compareTo((BigDecimal) y) < 0;
			} else if (y instanceof Short) {
				return ((Integer) x).intValue() < ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Integer) x).intValue() < ((Byte) y).byteValue();
			}
		} else if (x instanceof Long) {
			if (y instanceof Integer) {
				return ((Long) x).longValue() < ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Long) x).longValue() < ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Long) x).longValue() < ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Long) x).longValue() < ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Long) x).toString()).compareTo((BigInteger) y) < 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Long) x).toString()).compareTo((BigDecimal) y) < 0;
			} else if (y instanceof Short) {
				return ((Long) x).longValue() < ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Long) x).longValue() < ((Byte) y).byteValue();
			}
		} else if (x instanceof Float) {
			if (y instanceof Integer) {
				return ((Float) x).floatValue() < ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Float) x).floatValue() < ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Float) x).floatValue() < ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Float) x).floatValue() < ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigDecimal(((Float) x).toString()).compareTo(new BigDecimal((BigInteger) y)) < 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Float) x).toString()).compareTo((BigDecimal) y) < 0;
			} else if (y instanceof Short) {
				return ((Float) x).floatValue() < ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Float) x).floatValue() < ((Byte) y).byteValue();
			}
		} else if (x instanceof Double) {
			if (y instanceof Integer) {
				return ((Double) x).doubleValue() < ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Double) x).doubleValue() < ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Double) x).doubleValue() < ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Double) x).doubleValue() < ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigDecimal(((Double) x).toString()).compareTo(new BigDecimal((BigInteger) y)) < 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Double) x).toString()).compareTo((BigDecimal) y) < 0;
			} else if (y instanceof Short) {
				return ((Double) x).doubleValue() < ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Double) x).doubleValue() < ((Byte) y).byteValue();
			}
		} else if (x instanceof BigInteger) {
			if (y instanceof Integer) {
				return ((BigInteger) x).compareTo(new BigInteger(((Integer) y).toString())) < 0;
			} else if (y instanceof Long) {
				return ((BigInteger) x).compareTo(new BigInteger(((Long) y).toString())) < 0;
			} else if (y instanceof Float) {
				return (new BigDecimal((BigInteger) x)).compareTo(new BigDecimal(((Float) y).toString())) < 0;
			} else if (y instanceof Double) {
				return (new BigDecimal((BigInteger) x)).compareTo(new BigDecimal(((Double) y).toString())) < 0;
			} else if (y instanceof BigInteger) {
				return ((BigInteger) x).compareTo((BigInteger) y) < 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal((BigInteger) x).compareTo((BigDecimal) y) < 0;
			} else if (y instanceof Short) {
				return ((BigInteger) x).compareTo(new BigInteger(((Short) y).toString())) < 0;
			} else if (y instanceof Byte) {
				return ((BigInteger) x).compareTo(new BigInteger(((Byte) y).toString())) < 0;
			}
		} else if (x instanceof BigDecimal) {
			if (y instanceof Integer) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Integer) y).toString())) < 0;
			} else if (y instanceof Long) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Long) y).toString())) < 0;
			} else if (y instanceof Float) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Float) y).toString())) < 0;
			} else if (y instanceof Double) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Double) y).toString())) < 0;
			} else if (y instanceof BigInteger) {
				return ((BigDecimal) x).compareTo(new BigDecimal((BigInteger) y)) < 0;
			} else if (y instanceof BigDecimal) {
				return ((BigDecimal) x).compareTo((BigDecimal) y) < 0;
			} else if (y instanceof Short) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Short) y).toString())) < 0;
			} else if (y instanceof Byte) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Byte) y).toString())) < 0;
			}
		} else if (x instanceof Short) {
			if (y instanceof Integer) {
				return ((Short) x).shortValue() < ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Short) x).shortValue() < ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Short) x).shortValue() < ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Short) x).shortValue() < ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Short) x).toString()).compareTo((BigInteger) y) < 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Short) x).toString()).compareTo((BigDecimal) y) < 0;
			} else if (y instanceof Short) {
				return ((Short) x).shortValue() < ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Short) x).shortValue() < ((Byte) y).byteValue();
			}
		} else if (x instanceof Byte) {
			if (y instanceof Integer) {
				return ((Byte) x).byteValue() < ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Byte) x).byteValue() < ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Byte) x).byteValue() < ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Byte) x).byteValue() < ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Byte) x).toString()).compareTo((BigInteger) y) < 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Byte) x).toString()).compareTo((BigDecimal) y) < 0;
			} else if (y instanceof Short) {
				return ((Byte) x).byteValue() < ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Byte) x).byteValue() < ((Byte) y).byteValue();
			}
		}
		throw new OgnlException("Unsupported logical expression(<) object. x:" + x.getClass().getName() + ", y:" + y.getClass().getName());
	}

	/**
	 * 小于等于
	 */
	public static boolean numberLessThanEqual(Number x, Number y) {
		if (x instanceof Integer) {
			if (y instanceof Integer) {
				return ((Integer) x).intValue() <= ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Integer) x).intValue() <= ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Integer) x).intValue() <= ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Integer) x).intValue() <= ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Integer) x).toString()).compareTo((BigInteger) y) <= 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Integer) x).toString()).compareTo((BigDecimal) y) <= 0;
			} else if (y instanceof Short) {
				return ((Integer) x).intValue() <= ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Integer) x).intValue() <= ((Byte) y).byteValue();
			}
		} else if (x instanceof Long) {
			if (y instanceof Integer) {
				return ((Long) x).longValue() <= ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Long) x).longValue() <= ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Long) x).longValue() <= ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Long) x).longValue() <= ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Long) x).toString()).compareTo((BigInteger) y) <= 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Long) x).toString()).compareTo((BigDecimal) y) <= 0;
			} else if (y instanceof Short) {
				return ((Long) x).longValue() <= ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Long) x).longValue() <= ((Byte) y).byteValue();
			}
		} else if (x instanceof Float) {
			if (y instanceof Integer) {
				return ((Float) x).floatValue() <= ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Float) x).floatValue() <= ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Float) x).floatValue() <= ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Float) x).floatValue() <= ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigDecimal(((Float) x).toString()).compareTo(new BigDecimal((BigInteger) y)) <= 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Float) x).toString()).compareTo((BigDecimal) y) <= 0;
			} else if (y instanceof Short) {
				return ((Float) x).floatValue() <= ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Float) x).floatValue() <= ((Byte) y).byteValue();
			}
		} else if (x instanceof Double) {
			if (y instanceof Integer) {
				return ((Double) x).doubleValue() <= ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Double) x).doubleValue() <= ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Double) x).doubleValue() <= ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Double) x).doubleValue() <= ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigDecimal(((Double) x).toString()).compareTo(new BigDecimal((BigInteger) y)) <= 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Double) x).toString()).compareTo((BigDecimal) y) <= 0;
			} else if (y instanceof Short) {
				return ((Double) x).doubleValue() <= ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Double) x).doubleValue() <= ((Byte) y).byteValue();
			}
		} else if (x instanceof BigInteger) {
			if (y instanceof Integer) {
				return ((BigInteger) x).compareTo(new BigInteger(((Integer) y).toString())) <= 0;
			} else if (y instanceof Long) {
				return ((BigInteger) x).compareTo(new BigInteger(((Long) y).toString())) <= 0;
			} else if (y instanceof Float) {
				return (new BigDecimal((BigInteger) x)).compareTo(new BigDecimal(((Float) y).toString())) <= 0;
			} else if (y instanceof Double) {
				return (new BigDecimal((BigInteger) x)).compareTo(new BigDecimal(((Double) y).toString())) <= 0;
			} else if (y instanceof BigInteger) {
				return ((BigInteger) x).compareTo((BigInteger) y) <= 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal((BigInteger) x).compareTo((BigDecimal) y) <= 0;
			} else if (y instanceof Short) {
				return ((BigInteger) x).compareTo(new BigInteger(((Short) y).toString())) <= 0;
			} else if (y instanceof Byte) {
				return ((BigInteger) x).compareTo(new BigInteger(((Byte) y).toString())) <= 0;
			}
		} else if (x instanceof BigDecimal) {
			if (y instanceof Integer) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Integer) y).toString())) <= 0;
			} else if (y instanceof Long) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Long) y).toString())) <= 0;
			} else if (y instanceof Float) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Float) y).toString())) <= 0;
			} else if (y instanceof Double) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Double) y).toString())) <= 0;
			} else if (y instanceof BigInteger) {
				return ((BigDecimal) x).compareTo(new BigDecimal((BigInteger) y)) <= 0;
			} else if (y instanceof BigDecimal) {
				return ((BigDecimal) x).compareTo((BigDecimal) y) <= 0;
			} else if (y instanceof Short) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Short) y).toString())) <= 0;
			} else if (y instanceof Byte) {
				return ((BigDecimal) x).compareTo(new BigDecimal(((Byte) y).toString())) <= 0;
			}
		} else if (x instanceof Short) {
			if (y instanceof Integer) {
				return ((Short) x).shortValue() <= ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Short) x).shortValue() <= ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Short) x).shortValue() <= ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Short) x).shortValue() <= ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Short) x).toString()).compareTo((BigInteger) y) <= 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Short) x).toString()).compareTo((BigDecimal) y) <= 0;
			} else if (y instanceof Short) {
				return ((Short) x).shortValue() <= ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Short) x).shortValue() <= ((Byte) y).byteValue();
			}
		} else if (x instanceof Byte) {
			if (y instanceof Integer) {
				return ((Byte) x).byteValue() <= ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Byte) x).byteValue() <= ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Byte) x).byteValue() <= ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Byte) x).byteValue() <= ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Byte) x).toString()).compareTo((BigInteger) y) <= 0;
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Byte) x).toString()).compareTo((BigDecimal) y) <= 0;
			} else if (y instanceof Short) {
				return ((Byte) x).byteValue() <= ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Byte) x).byteValue() <= ((Byte) y).byteValue();
			}
		}
		throw new OgnlException("Unsupported logical expression(<=) object. x:" + x.getClass().getName() + ", y:" + y.getClass().getName());
	}

	// sql.data, sql.time, data, Timestamp

	/**
	 * 相等
	 */
	public static boolean dateEqual(java.util.Date x, java.util.Date y) {
		if (x instanceof java.util.Date) {
			if (y instanceof java.util.Date) {
				return ((java.util.Date) x).getTime() == ((java.util.Date) y).getTime();
			} else if (y instanceof java.sql.Timestamp) {
				return ((java.util.Date) x).getTime() == ((java.sql.Timestamp) y).getTime();
			} else if (y instanceof java.sql.Date) {
				return ((java.util.Date) x).getTime() == ((java.sql.Date) y).getTime();
			} else if (y instanceof java.sql.Time) {
				return ((java.util.Date) x).getTime() == ((java.sql.Time) y).getTime();
			}
		} else if (x instanceof java.sql.Timestamp) {
			if (y instanceof java.util.Date) {
				return ((java.sql.Timestamp) x).getTime() == ((java.util.Date) y).getTime();
			} else if (y instanceof java.sql.Timestamp) {
				// return ((java.sql.Timestamp) x).getTime() ==
				// ((java.sql.Timestamp) y).getTime();
				return ((java.sql.Timestamp) x).compareTo((java.sql.Timestamp) y) == 0;
			} else if (y instanceof java.sql.Date) {
				return ((java.sql.Timestamp) x).getTime() == ((java.sql.Date) y).getTime();
			} else if (y instanceof java.sql.Time) {
				return ((java.sql.Timestamp) x).getTime() == ((java.sql.Time) y).getTime();
			}
		} else if (x instanceof java.sql.Date) {
			if (y instanceof java.util.Date) {
				return ((java.sql.Date) x).getTime() == ((java.util.Date) y).getTime();
			} else if (y instanceof java.sql.Timestamp) {
				return ((java.sql.Date) x).getTime() == ((java.sql.Timestamp) y).getTime();
			} else if (y instanceof java.sql.Date) {
				return ((java.sql.Date) x).getTime() == ((java.sql.Date) y).getTime();
			} else if (y instanceof java.sql.Time) {
				return ((java.sql.Date) x).getTime() == ((java.sql.Time) y).getTime();
			}
		} else if (x instanceof java.sql.Time) {
			if (y instanceof java.util.Date) {
				return ((java.sql.Time) x).getTime() == ((java.util.Date) y).getTime();
			} else if (y instanceof java.sql.Timestamp) {
				return ((java.sql.Time) x).getTime() == ((java.sql.Timestamp) y).getTime();
			} else if (y instanceof java.sql.Date) {
				return ((java.sql.Time) x).getTime() == ((java.sql.Date) y).getTime();
			} else if (y instanceof java.sql.Time) {
				return ((java.sql.Time) x).getTime() == ((java.sql.Time) y).getTime();
			}
		}
		throw new OgnlException("Unsupported logical expression(==) object. x:" + x.getClass().getName() + ", y:" + y.getClass().getName());
	}

	/**
	 * 不相等
	 */
	public static boolean dateNotEqual(java.util.Date x, java.util.Date y) {
		if (x instanceof java.util.Date) {
			if (y instanceof java.util.Date) {
				return ((java.util.Date) x).getTime() != ((java.util.Date) y).getTime();
			} else if (y instanceof java.sql.Timestamp) {
				return ((java.util.Date) x).getTime() != ((java.sql.Timestamp) y).getTime();
			} else if (y instanceof java.sql.Date) {
				return ((java.util.Date) x).getTime() != ((java.sql.Date) y).getTime();
			} else if (y instanceof java.sql.Time) {
				return ((java.util.Date) x).getTime() != ((java.sql.Time) y).getTime();
			}
		} else if (x instanceof java.sql.Timestamp) {
			if (y instanceof java.util.Date) {
				return ((java.sql.Timestamp) x).getTime() != ((java.util.Date) y).getTime();
			} else if (y instanceof java.sql.Timestamp) {
				// return ((java.sql.Timestamp) x).getTime() !=
				// ((java.sql.Timestamp) y).getTime();
				return ((java.sql.Timestamp) x).compareTo((java.sql.Timestamp) y) != 0;
			} else if (y instanceof java.sql.Date) {
				return ((java.sql.Timestamp) x).getTime() != ((java.sql.Date) y).getTime();
			} else if (y instanceof java.sql.Time) {
				return ((java.sql.Timestamp) x).getTime() != ((java.sql.Time) y).getTime();
			}
		} else if (x instanceof java.sql.Date) {
			if (y instanceof java.util.Date) {
				return ((java.sql.Date) x).getTime() != ((java.util.Date) y).getTime();
			} else if (y instanceof java.sql.Timestamp) {
				return ((java.sql.Date) x).getTime() != ((java.sql.Timestamp) y).getTime();
			} else if (y instanceof java.sql.Date) {
				return ((java.sql.Date) x).getTime() != ((java.sql.Date) y).getTime();
			} else if (y instanceof java.sql.Time) {
				return ((java.sql.Date) x).getTime() != ((java.sql.Time) y).getTime();
			}
		} else if (x instanceof java.sql.Time) {
			if (y instanceof java.util.Date) {
				return ((java.sql.Time) x).getTime() != ((java.util.Date) y).getTime();
			} else if (y instanceof java.sql.Timestamp) {
				return ((java.sql.Time) x).getTime() != ((java.sql.Timestamp) y).getTime();
			} else if (y instanceof java.sql.Date) {
				return ((java.sql.Time) x).getTime() != ((java.sql.Date) y).getTime();
			} else if (y instanceof java.sql.Time) {
				return ((java.sql.Time) x).getTime() != ((java.sql.Time) y).getTime();
			}
		}
		throw new OgnlException("Unsupported logical expression(!=) object. x:" + x.getClass().getName() + ", y:" + y.getClass().getName());
	}

	/**
	 * 大于
	 */
	public static boolean dateMoreThan(java.util.Date x, java.util.Date y) {
		if (x instanceof java.util.Date) {
			if (y instanceof java.util.Date) {
				return ((java.util.Date) x).getTime() > ((java.util.Date) y).getTime();
			} else if (y instanceof java.sql.Timestamp) {
				return ((java.util.Date) x).getTime() > ((java.sql.Timestamp) y).getTime();
			} else if (y instanceof java.sql.Date) {
				return ((java.util.Date) x).getTime() > ((java.sql.Date) y).getTime();
			} else if (y instanceof java.sql.Time) {
				return ((java.util.Date) x).getTime() > ((java.sql.Time) y).getTime();
			}
		} else if (x instanceof java.sql.Timestamp) {
			if (y instanceof java.util.Date) {
				return ((java.sql.Timestamp) x).getTime() > ((java.util.Date) y).getTime();
			} else if (y instanceof java.sql.Timestamp) {
				// return ((java.sql.Timestamp) x).getTime() >
				// ((java.sql.Timestamp) y).getTime();
				return ((java.sql.Timestamp) x).compareTo((java.sql.Timestamp) y) > 0;
			} else if (y instanceof java.sql.Date) {
				return ((java.sql.Timestamp) x).getTime() > ((java.sql.Date) y).getTime();
			} else if (y instanceof java.sql.Time) {
				return ((java.sql.Timestamp) x).getTime() > ((java.sql.Time) y).getTime();
			}
		} else if (x instanceof java.sql.Date) {
			if (y instanceof java.util.Date) {
				return ((java.sql.Date) x).getTime() > ((java.util.Date) y).getTime();
			} else if (y instanceof java.sql.Timestamp) {
				return ((java.sql.Date) x).getTime() > ((java.sql.Timestamp) y).getTime();
			} else if (y instanceof java.sql.Date) {
				return ((java.sql.Date) x).getTime() > ((java.sql.Date) y).getTime();
			} else if (y instanceof java.sql.Time) {
				return ((java.sql.Date) x).getTime() > ((java.sql.Time) y).getTime();
			}
		} else if (x instanceof java.sql.Time) {
			if (y instanceof java.util.Date) {
				return ((java.sql.Time) x).getTime() > ((java.util.Date) y).getTime();
			} else if (y instanceof java.sql.Timestamp) {
				return ((java.sql.Time) x).getTime() > ((java.sql.Timestamp) y).getTime();
			} else if (y instanceof java.sql.Date) {
				return ((java.sql.Time) x).getTime() > ((java.sql.Date) y).getTime();
			} else if (y instanceof java.sql.Time) {
				return ((java.sql.Time) x).getTime() > ((java.sql.Time) y).getTime();
			}
		}
		throw new OgnlException("Unsupported logical expression(>) object. x:" + x.getClass().getName() + ", y:" + y.getClass().getName());
	}

	/**
	 * 大于等于
	 */
	public static boolean dateMoreThanEqual(java.util.Date x, java.util.Date y) {
		if (x instanceof java.util.Date) {
			if (y instanceof java.util.Date) {
				return ((java.util.Date) x).getTime() >= ((java.util.Date) y).getTime();
			} else if (y instanceof java.sql.Timestamp) {
				return ((java.util.Date) x).getTime() >= ((java.sql.Timestamp) y).getTime();
			} else if (y instanceof java.sql.Date) {
				return ((java.util.Date) x).getTime() >= ((java.sql.Date) y).getTime();
			} else if (y instanceof java.sql.Time) {
				return ((java.util.Date) x).getTime() >= ((java.sql.Time) y).getTime();
			}
		} else if (x instanceof java.sql.Timestamp) {
			if (y instanceof java.util.Date) {
				return ((java.sql.Timestamp) x).getTime() >= ((java.util.Date) y).getTime();
			} else if (y instanceof java.sql.Timestamp) {
				// return ((java.sql.Timestamp) x).getTime() >=
				// ((java.sql.Timestamp) y).getTime();
				return ((java.sql.Timestamp) x).compareTo((java.sql.Timestamp) y) >= 0;
			} else if (y instanceof java.sql.Date) {
				return ((java.sql.Timestamp) x).getTime() >= ((java.sql.Date) y).getTime();
			} else if (y instanceof java.sql.Time) {
				return ((java.sql.Timestamp) x).getTime() >= ((java.sql.Time) y).getTime();
			}
		} else if (x instanceof java.sql.Date) {
			if (y instanceof java.util.Date) {
				return ((java.sql.Date) x).getTime() >= ((java.util.Date) y).getTime();
			} else if (y instanceof java.sql.Timestamp) {
				return ((java.sql.Date) x).getTime() >= ((java.sql.Timestamp) y).getTime();
			} else if (y instanceof java.sql.Date) {
				return ((java.sql.Date) x).getTime() >= ((java.sql.Date) y).getTime();
			} else if (y instanceof java.sql.Time) {
				return ((java.sql.Date) x).getTime() >= ((java.sql.Time) y).getTime();
			}
		} else if (x instanceof java.sql.Time) {
			if (y instanceof java.util.Date) {
				return ((java.sql.Time) x).getTime() >= ((java.util.Date) y).getTime();
			} else if (y instanceof java.sql.Timestamp) {
				return ((java.sql.Time) x).getTime() >= ((java.sql.Timestamp) y).getTime();
			} else if (y instanceof java.sql.Date) {
				return ((java.sql.Time) x).getTime() >= ((java.sql.Date) y).getTime();
			} else if (y instanceof java.sql.Time) {
				return ((java.sql.Time) x).getTime() >= ((java.sql.Time) y).getTime();
			}
		}
		throw new OgnlException("Unsupported logical expression(>=) object. x:" + x.getClass().getName() + ", y:" + y.getClass().getName());
	}

	/**
	 * 小于
	 */
	public static boolean dateLessThan(java.util.Date x, java.util.Date y) {
		if (x instanceof java.util.Date) {
			if (y instanceof java.util.Date) {
				return ((java.util.Date) x).getTime() < ((java.util.Date) y).getTime();
			} else if (y instanceof java.sql.Timestamp) {
				return ((java.util.Date) x).getTime() < ((java.sql.Timestamp) y).getTime();
			} else if (y instanceof java.sql.Date) {
				return ((java.util.Date) x).getTime() < ((java.sql.Date) y).getTime();
			} else if (y instanceof java.sql.Time) {
				return ((java.util.Date) x).getTime() < ((java.sql.Time) y).getTime();
			}
		} else if (x instanceof java.sql.Timestamp) {
			if (y instanceof java.util.Date) {
				return ((java.sql.Timestamp) x).getTime() < ((java.util.Date) y).getTime();
			} else if (y instanceof java.sql.Timestamp) {
				// return ((java.sql.Timestamp) x).getTime() <
				// ((java.sql.Timestamp) y).getTime();
				return ((java.sql.Timestamp) x).compareTo((java.sql.Timestamp) y) < 0;
			} else if (y instanceof java.sql.Date) {
				return ((java.sql.Timestamp) x).getTime() < ((java.sql.Date) y).getTime();
			} else if (y instanceof java.sql.Time) {
				return ((java.sql.Timestamp) x).getTime() < ((java.sql.Time) y).getTime();
			}
		} else if (x instanceof java.sql.Date) {
			if (y instanceof java.util.Date) {
				return ((java.sql.Date) x).getTime() < ((java.util.Date) y).getTime();
			} else if (y instanceof java.sql.Timestamp) {
				return ((java.sql.Date) x).getTime() < ((java.sql.Timestamp) y).getTime();
			} else if (y instanceof java.sql.Date) {
				return ((java.sql.Date) x).getTime() < ((java.sql.Date) y).getTime();
			} else if (y instanceof java.sql.Time) {
				return ((java.sql.Date) x).getTime() < ((java.sql.Time) y).getTime();
			}
		} else if (x instanceof java.sql.Time) {
			if (y instanceof java.util.Date) {
				return ((java.sql.Time) x).getTime() < ((java.util.Date) y).getTime();
			} else if (y instanceof java.sql.Timestamp) {
				return ((java.sql.Time) x).getTime() < ((java.sql.Timestamp) y).getTime();
			} else if (y instanceof java.sql.Date) {
				return ((java.sql.Time) x).getTime() < ((java.sql.Date) y).getTime();
			} else if (y instanceof java.sql.Time) {
				return ((java.sql.Time) x).getTime() < ((java.sql.Time) y).getTime();
			}
		}
		throw new OgnlException("Unsupported logical expression(<) object. x:" + x.getClass().getName() + ", y:" + y.getClass().getName());
	}

	/**
	 * 小于等于
	 */
	public static boolean dateLessThanEqual(java.util.Date x, java.util.Date y) {
		if (x instanceof java.util.Date) {
			if (y instanceof java.util.Date) {
				return ((java.util.Date) x).getTime() <= ((java.util.Date) y).getTime();
			} else if (y instanceof java.sql.Timestamp) {
				return ((java.util.Date) x).getTime() <= ((java.sql.Timestamp) y).getTime();
			} else if (y instanceof java.sql.Date) {
				return ((java.util.Date) x).getTime() <= ((java.sql.Date) y).getTime();
			} else if (y instanceof java.sql.Time) {
				return ((java.util.Date) x).getTime() <= ((java.sql.Time) y).getTime();
			}
		} else if (x instanceof java.sql.Timestamp) {
			if (y instanceof java.util.Date) {
				return ((java.sql.Timestamp) x).getTime() <= ((java.util.Date) y).getTime();
			} else if (y instanceof java.sql.Timestamp) {
				// return ((java.sql.Timestamp) x).getTime() <=
				// ((java.sql.Timestamp) y).getTime();
				return ((java.sql.Timestamp) x).compareTo((java.sql.Timestamp) y) <= 0;
			} else if (y instanceof java.sql.Date) {
				return ((java.sql.Timestamp) x).getTime() <= ((java.sql.Date) y).getTime();
			} else if (y instanceof java.sql.Time) {
				return ((java.sql.Timestamp) x).getTime() <= ((java.sql.Time) y).getTime();
			}
		} else if (x instanceof java.sql.Date) {
			if (y instanceof java.util.Date) {
				return ((java.sql.Date) x).getTime() <= ((java.util.Date) y).getTime();
			} else if (y instanceof java.sql.Timestamp) {
				return ((java.sql.Date) x).getTime() <= ((java.sql.Timestamp) y).getTime();
			} else if (y instanceof java.sql.Date) {
				return ((java.sql.Date) x).getTime() <= ((java.sql.Date) y).getTime();
			} else if (y instanceof java.sql.Time) {
				return ((java.sql.Date) x).getTime() <= ((java.sql.Time) y).getTime();
			}
		} else if (x instanceof java.sql.Time) {
			if (y instanceof java.util.Date) {
				return ((java.sql.Time) x).getTime() <= ((java.util.Date) y).getTime();
			} else if (y instanceof java.sql.Timestamp) {
				return ((java.sql.Time) x).getTime() <= ((java.sql.Timestamp) y).getTime();
			} else if (y instanceof java.sql.Date) {
				return ((java.sql.Time) x).getTime() <= ((java.sql.Date) y).getTime();
			} else if (y instanceof java.sql.Time) {
				return ((java.sql.Time) x).getTime() <= ((java.sql.Time) y).getTime();
			}
		}
		throw new OgnlException("Unsupported logical expression(<=) object. x:" + x.getClass().getName() + ", y:" + y.getClass().getName());
	}

	public static void main(String[] args) throws Throwable {
		Number[] xx = { 0x02, 1.4D, 1.3F, 5, 2L, new Short("3"), new BigDecimal(1.4d), new BigInteger("2") };
		// Number[] xx = { 2, 2D, 2F, 2, 2L, new Short("2"), new BigDecimal(2),
		// new BigInteger("2") };
		for (int i = 0; i < xx.length; i++) {
			for (int j = 0; j < xx.length; j++) {
				System.out.println(xx[i] + "==" + xx[j] + ":\t" + numberEqual(xx[i], xx[j]));
				System.out.println(xx[i] + "!=" + xx[j] + ":\t" + numberNotEqual(xx[i], xx[j]));
				System.out.println(xx[i] + ">" + xx[j] + ":\t" + numberMoreThan(xx[i], xx[j]));
				System.out.println(xx[i] + ">=" + xx[j] + ":\t" + numberMoreThanEqual(xx[i], xx[j]));
				System.out.println(xx[i] + "<" + xx[j] + ":\t" + numberLessThan(xx[i], xx[j]));
				System.out.println(xx[i] + "<=" + xx[j] + ":\t" + numberLessThanEqual(xx[i], xx[j]));
			}
			System.out.println();
		}

		java.sql.Timestamp t1 = new java.sql.Timestamp(System.currentTimeMillis());
		// Thread.currentThread().sleep(100L);
		java.sql.Timestamp t2 = new java.sql.Timestamp(System.currentTimeMillis());

		System.out.println(t1.compareTo(t2));
	}
}
