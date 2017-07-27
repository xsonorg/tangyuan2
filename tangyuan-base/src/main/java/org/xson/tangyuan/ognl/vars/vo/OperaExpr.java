package org.xson.tangyuan.ognl.vars.vo;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.xson.tangyuan.ognl.OgnlException;

/**
 * 运算表达式计算<br />
 * 支持Byte, Double, Float, Integer, Long, Short, BigDecimal, BigInteger
 */
public class OperaExpr {

	public static int	scale			= 4;
	public static int	roundingMode	= BigDecimal.ROUND_HALF_DOWN;

	public static Number add(Number x, Number y) {
		if (x instanceof Integer) {
			if (y instanceof Integer) {
				return ((Integer) x).intValue() + ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Integer) x).intValue() + ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Integer) x).intValue() + ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Integer) x).intValue() + ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Integer) x).toString()).add((BigInteger) y);
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Integer) x).toString()).add((BigDecimal) y);
			} else if (y instanceof Short) {
				return ((Integer) x).intValue() + ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Integer) x).intValue() + ((Byte) y).byteValue();
			}
		} else if (x instanceof Long) {
			if (y instanceof Integer) {
				return ((Long) x).longValue() + ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Long) x).longValue() + ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Long) x).longValue() + ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Long) x).longValue() + ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Long) x).toString()).add((BigInteger) y);
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Long) x).toString()).add((BigDecimal) y);
			} else if (y instanceof Short) {
				return ((Long) x).longValue() + ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Long) x).longValue() + ((Byte) y).byteValue();
			}
		} else if (x instanceof Float) {
			if (y instanceof Integer) {
				return ((Float) x).floatValue() + ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Float) x).floatValue() + ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Float) x).floatValue() + ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Float) x).floatValue() + ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigDecimal(((Float) x).toString()).add(new BigDecimal((BigInteger) y));
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Float) x).toString()).add((BigDecimal) y);
			} else if (y instanceof Short) {
				return ((Float) x).floatValue() + ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Float) x).floatValue() + ((Byte) y).byteValue();
			}
		} else if (x instanceof Double) {
			if (y instanceof Integer) {
				return ((Double) x).doubleValue() + ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Double) x).doubleValue() + ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Double) x).doubleValue() + ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Double) x).doubleValue() + ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigDecimal(((Double) x).toString()).add(new BigDecimal((BigInteger) y));
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Double) x).toString()).add((BigDecimal) y);
			} else if (y instanceof Short) {
				return ((Double) x).doubleValue() + ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Double) x).doubleValue() + ((Byte) y).byteValue();
			}
		} else if (x instanceof BigInteger) {
			if (y instanceof Integer) {
				return ((BigInteger) x).add(new BigInteger(((Integer) y).toString()));
			} else if (y instanceof Long) {
				return ((BigInteger) x).add(new BigInteger(((Long) y).toString()));
			} else if (y instanceof Float) {
				return (new BigDecimal((BigInteger) x)).add(new BigDecimal(((Float) y).toString()));
			} else if (y instanceof Double) {
				return (new BigDecimal((BigInteger) x)).add(new BigDecimal(((Double) y).toString()));
			} else if (y instanceof BigInteger) {
				return ((BigInteger) x).add((BigInteger) y);
			} else if (y instanceof BigDecimal) {
				return new BigDecimal((BigInteger) x).add((BigDecimal) y);
			} else if (y instanceof Short) {
				return ((BigInteger) x).add(new BigInteger(((Short) y).toString()));
			} else if (y instanceof Byte) {
				return ((BigInteger) x).add(new BigInteger(((Byte) y).toString()));
			}
		} else if (x instanceof BigDecimal) {
			if (y instanceof Integer) {
				return ((BigDecimal) x).add(new BigDecimal(((Integer) y).toString()));
			} else if (y instanceof Long) {
				return ((BigDecimal) x).add(new BigDecimal(((Long) y).toString()));
			} else if (y instanceof Float) {
				return ((BigDecimal) x).add(new BigDecimal(((Float) y).toString()));
			} else if (y instanceof Double) {
				return ((BigDecimal) x).add(new BigDecimal(((Double) y).toString()));
			} else if (y instanceof BigInteger) {
				return ((BigDecimal) x).add(new BigDecimal((BigInteger) y));
			} else if (y instanceof BigDecimal) {
				return ((BigDecimal) x).add((BigDecimal) y);
			} else if (y instanceof Short) {
				return ((BigDecimal) x).add(new BigDecimal(((Short) y).toString()));
			} else if (y instanceof Byte) {
				return ((BigDecimal) x).add(new BigDecimal(((Byte) y).toString()));
			}
		} else if (x instanceof Short) {
			if (y instanceof Integer) {
				return ((Short) x).shortValue() + ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Short) x).shortValue() + ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Short) x).shortValue() + ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Short) x).shortValue() + ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Short) x).toString()).add((BigInteger) y);
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Short) x).toString()).add((BigDecimal) y);
			} else if (y instanceof Short) {
				return ((Short) x).shortValue() + ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Short) x).shortValue() + ((Byte) y).byteValue();
			}
		} else if (x instanceof Byte) {
			if (y instanceof Integer) {
				return ((Byte) x).byteValue() + ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Byte) x).byteValue() + ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Byte) x).byteValue() + ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Byte) x).byteValue() + ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Byte) x).toString()).add((BigInteger) y);
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Byte) x).toString()).add((BigDecimal) y);
			} else if (y instanceof Short) {
				return ((Byte) x).byteValue() + ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Byte) x).byteValue() + ((Byte) y).byteValue();
			}
		}
		throw new OgnlException("Unsupported operation expression(add) object. x:" + x.getClass().getName() + ", y:" + y.getClass().getName());
	}

	/**
	 * 减法
	 */
	public static Number minus(Number x, Number y) {
		if (x instanceof Integer) {
			if (y instanceof Integer) {
				return ((Integer) x).intValue() - ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Integer) x).intValue() - ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Integer) x).intValue() - ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Integer) x).intValue() - ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Integer) x).toString()).subtract((BigInteger) y);
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Integer) x).toString()).subtract((BigDecimal) y);
			} else if (y instanceof Short) {
				return ((Integer) x).intValue() - ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Integer) x).intValue() - ((Byte) y).byteValue();
			}
		} else if (x instanceof Long) {
			if (y instanceof Integer) {
				return ((Long) x).longValue() - ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Long) x).longValue() - ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Long) x).longValue() - ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Long) x).longValue() - ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Long) x).toString()).subtract((BigInteger) y);
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Long) x).toString()).subtract((BigDecimal) y);
			} else if (y instanceof Short) {
				return ((Long) x).longValue() - ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Long) x).longValue() - ((Byte) y).byteValue();
			}
		} else if (x instanceof Float) {
			if (y instanceof Integer) {
				return ((Float) x).floatValue() - ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Float) x).floatValue() - ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Float) x).floatValue() - ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Float) x).floatValue() - ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigDecimal(((Float) x).toString()).subtract(new BigDecimal((BigInteger) y));
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Float) x).toString()).subtract((BigDecimal) y);
			} else if (y instanceof Short) {
				return ((Float) x).floatValue() - ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Float) x).floatValue() - ((Byte) y).byteValue();
			}
		} else if (x instanceof Double) {
			if (y instanceof Integer) {
				return ((Double) x).doubleValue() - ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Double) x).doubleValue() - ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Double) x).doubleValue() - ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Double) x).doubleValue() - ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigDecimal(((Double) x).toString()).subtract(new BigDecimal((BigInteger) y));
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Double) x).toString()).subtract((BigDecimal) y);
			} else if (y instanceof Short) {
				return ((Double) x).doubleValue() - ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Double) x).doubleValue() - ((Byte) y).byteValue();
			}
		} else if (x instanceof BigInteger) {
			if (y instanceof Integer) {
				return ((BigInteger) x).subtract(new BigInteger(((Integer) y).toString()));
			} else if (y instanceof Long) {
				return ((BigInteger) x).subtract(new BigInteger(((Long) y).toString()));
			} else if (y instanceof Float) {
				return (new BigDecimal((BigInteger) x)).subtract(new BigDecimal(((Float) y).toString()));
			} else if (y instanceof Double) {
				return (new BigDecimal((BigInteger) x)).subtract(new BigDecimal(((Double) y).toString()));
			} else if (y instanceof BigInteger) {
				return ((BigInteger) x).subtract((BigInteger) y);
			} else if (y instanceof BigDecimal) {
				return new BigDecimal((BigInteger) x).subtract((BigDecimal) y);
			} else if (y instanceof Short) {
				return ((BigInteger) x).subtract(new BigInteger(((Short) y).toString()));
			} else if (y instanceof Byte) {
				return ((BigInteger) x).subtract(new BigInteger(((Byte) y).toString()));
			}
		} else if (x instanceof BigDecimal) {
			if (y instanceof Integer) {
				return ((BigDecimal) x).subtract(new BigDecimal(((Integer) y).toString()));
			} else if (y instanceof Long) {
				return ((BigDecimal) x).subtract(new BigDecimal(((Long) y).toString()));
			} else if (y instanceof Float) {
				return ((BigDecimal) x).subtract(new BigDecimal(((Float) y).toString()));
			} else if (y instanceof Double) {
				return ((BigDecimal) x).subtract(new BigDecimal(((Double) y).toString()));
			} else if (y instanceof BigInteger) {
				return ((BigDecimal) x).subtract(new BigDecimal((BigInteger) y));
			} else if (y instanceof BigDecimal) {
				return ((BigDecimal) x).subtract((BigDecimal) y);
			} else if (y instanceof Short) {
				return ((BigDecimal) x).subtract(new BigDecimal(((Short) y).toString()));
			} else if (y instanceof Byte) {
				return ((BigDecimal) x).subtract(new BigDecimal(((Byte) y).toString()));
			}
		} else if (x instanceof Short) {
			if (y instanceof Integer) {
				return ((Short) x).shortValue() - ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Short) x).shortValue() - ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Short) x).shortValue() - ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Short) x).shortValue() - ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Short) x).toString()).subtract((BigInteger) y);
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Short) x).toString()).subtract((BigDecimal) y);
			} else if (y instanceof Short) {
				return ((Short) x).shortValue() - ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Short) x).shortValue() - ((Byte) y).byteValue();
			}
		} else if (x instanceof Byte) {
			if (y instanceof Integer) {
				return ((Byte) x).byteValue() - ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Byte) x).byteValue() - ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Byte) x).byteValue() - ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Byte) x).byteValue() - ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Byte) x).toString()).subtract((BigInteger) y);
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Byte) x).toString()).subtract((BigDecimal) y);
			} else if (y instanceof Short) {
				return ((Byte) x).byteValue() - ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Byte) x).byteValue() - ((Byte) y).byteValue();
			}
		}
		throw new OgnlException("Unsupported operation expression(subtract) object. x:" + x.getClass().getName() + ", y:" + y.getClass().getName());
	}

	/**
	 * 乘法
	 */
	public static Number multiply(Number x, Number y) {
		if (x instanceof Integer) {
			if (y instanceof Integer) {
				return ((Integer) x).intValue() * ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Integer) x).intValue() * ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Integer) x).intValue() * ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Integer) x).intValue() * ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return ((BigInteger) y).multiply(new BigInteger(((Integer) x).toString()));
			} else if (y instanceof BigDecimal) {
				return ((BigDecimal) y).multiply(new BigDecimal(((Integer) x).toString()));
			} else if (y instanceof Short) {
				return ((Integer) x).intValue() * ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Integer) x).intValue() * ((Byte) y).byteValue();
			}
		} else if (x instanceof Long) {
			if (y instanceof Integer) {
				return ((Long) x).longValue() * ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Long) x).longValue() * ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Long) x).longValue() * ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Long) x).longValue() * ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return ((BigInteger) y).multiply(new BigInteger(((Long) x).toString()));
			} else if (y instanceof BigDecimal) {
				return ((BigDecimal) y).multiply(new BigDecimal(((Long) x).toString()));
			} else if (y instanceof Short) {
				return ((Long) x).longValue() * ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Long) x).longValue() * ((Byte) y).byteValue();
			}
		} else if (x instanceof Float) {
			if (y instanceof Integer) {
				return ((Float) x).floatValue() * ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Float) x).floatValue() * ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Float) x).floatValue() * ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Float) x).floatValue() * ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigDecimal(((Float) x).toString()).multiply(new BigDecimal((BigInteger) y));
			} else if (y instanceof BigDecimal) {
				return ((BigDecimal) y).multiply(new BigDecimal(((Float) x).toString()));
			} else if (y instanceof Short) {
				return ((Float) x).floatValue() * ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Float) x).floatValue() * ((Byte) y).byteValue();
			}
		} else if (x instanceof Double) {
			if (y instanceof Integer) {
				return ((Double) x).doubleValue() * ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Double) x).doubleValue() * ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Double) x).doubleValue() * ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Double) x).doubleValue() * ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigDecimal(((Double) x).toString()).multiply(new BigDecimal((BigInteger) y));
			} else if (y instanceof BigDecimal) {
				return ((BigDecimal) y).multiply(new BigDecimal(((Double) x).toString()));
			} else if (y instanceof Short) {
				return ((Double) x).doubleValue() * ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Double) x).doubleValue() * ((Byte) y).byteValue();
			}
		} else if (x instanceof BigInteger) {
			if (y instanceof Integer) {
				return ((BigInteger) x).multiply(new BigInteger(((Integer) y).toString()));
			} else if (y instanceof Long) {
				return ((BigInteger) x).multiply(new BigInteger(((Long) y).toString()));
			} else if (y instanceof Float) {
				return (new BigDecimal((BigInteger) x)).multiply(new BigDecimal(((Float) y).toString()));
			} else if (y instanceof Double) {
				return (new BigDecimal((BigInteger) x)).multiply(new BigDecimal(((Double) y).toString()));
			} else if (y instanceof BigInteger) {
				return ((BigInteger) x).multiply((BigInteger) y);
			} else if (y instanceof BigDecimal) {
				return ((BigDecimal) y).multiply(new BigDecimal((BigInteger) x));
			} else if (y instanceof Short) {
				return ((BigInteger) x).multiply(new BigInteger(((Short) y).toString()));
			} else if (y instanceof Byte) {
				return ((BigInteger) x).multiply(new BigInteger(((Byte) y).toString()));
			}
		} else if (x instanceof BigDecimal) {
			if (y instanceof Integer) {
				return ((BigDecimal) x).multiply(new BigDecimal(((Integer) y).toString()));
			} else if (y instanceof Long) {
				return ((BigDecimal) x).multiply(new BigDecimal(((Long) y).toString()));
			} else if (y instanceof Float) {
				return ((BigDecimal) x).multiply(new BigDecimal(((Float) y).toString()));
			} else if (y instanceof Double) {
				return ((BigDecimal) x).multiply(new BigDecimal(((Double) y).toString()));
			} else if (y instanceof BigInteger) {
				return ((BigDecimal) x).multiply(new BigDecimal((BigInteger) y));
			} else if (y instanceof BigDecimal) {
				return ((BigDecimal) x).multiply((BigDecimal) y);
			} else if (y instanceof Short) {
				return ((BigDecimal) x).multiply(new BigDecimal(((Short) y).toString()));
			} else if (y instanceof Byte) {
				return ((BigDecimal) x).multiply(new BigDecimal(((Byte) y).toString()));
			}
		} else if (x instanceof Short) {
			if (y instanceof Integer) {
				return ((Short) x).shortValue() * ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Short) x).shortValue() * ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Short) x).shortValue() * ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Short) x).shortValue() * ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return ((BigInteger) y).multiply(new BigInteger(((Short) x).toString()));
			} else if (y instanceof BigDecimal) {
				return ((BigDecimal) y).multiply(new BigDecimal(((Short) x).toString()));
			} else if (y instanceof Short) {
				return ((Short) x).shortValue() * ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Short) x).shortValue() * ((Byte) y).byteValue();
			}
		} else if (x instanceof Byte) {
			if (y instanceof Integer) {
				return ((Byte) x).byteValue() * ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Byte) x).byteValue() * ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Byte) x).byteValue() * ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Byte) x).byteValue() * ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return ((BigInteger) y).multiply(new BigInteger(((Byte) x).toString()));
			} else if (y instanceof BigDecimal) {
				return ((BigDecimal) y).multiply(new BigDecimal(((Byte) x).toString()));
			} else if (y instanceof Short) {
				return ((Byte) x).byteValue() * ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Byte) x).byteValue() * ((Byte) y).byteValue();
			}
		}
		throw new OgnlException("Unsupported operation expression(multiply) object. x:" + x.getClass().getName() + ", y:" + y.getClass().getName());
	}

	/**
	 * 除法
	 */
	public static Number divide(Number x, Number y) {
		if (x instanceof Integer) {
			if (y instanceof Integer) {
				return ((Integer) x).intValue() / ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Integer) x).intValue() / ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Integer) x).intValue() / ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Integer) x).intValue() / ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Integer) x).toString()).divide((BigInteger) y);
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Integer) x).toString()).divide((BigDecimal) y, scale, roundingMode);
			} else if (y instanceof Short) {
				return ((Integer) x).intValue() / ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Integer) x).intValue() / ((Byte) y).byteValue();
			}
		} else if (x instanceof Long) {
			if (y instanceof Integer) {
				return ((Long) x).longValue() / ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Long) x).longValue() / ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Long) x).longValue() / ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Long) x).longValue() / ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Long) x).toString()).divide((BigInteger) y);
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Long) x).toString()).divide((BigDecimal) y, scale, roundingMode);
			} else if (y instanceof Short) {
				return ((Long) x).longValue() / ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Long) x).longValue() / ((Byte) y).byteValue();
			}
		} else if (x instanceof Float) {
			if (y instanceof Integer) {
				return ((Float) x).floatValue() / ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Float) x).floatValue() / ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Float) x).floatValue() / ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Float) x).floatValue() / ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigDecimal(((Float) x).toString()).divide(new BigDecimal((BigInteger) y), scale, roundingMode);
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Float) x).toString()).divide((BigDecimal) y, scale, roundingMode);
			} else if (y instanceof Short) {
				return ((Float) x).floatValue() / ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Float) x).floatValue() / ((Byte) y).byteValue();
			}
		} else if (x instanceof Double) {
			if (y instanceof Integer) {
				return ((Double) x).doubleValue() / ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Double) x).doubleValue() / ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Double) x).doubleValue() / ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Double) x).doubleValue() / ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigDecimal(((Double) x).toString()).divide(new BigDecimal((BigInteger) y), scale, roundingMode);
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Double) x).toString()).divide((BigDecimal) y, scale, roundingMode);
			} else if (y instanceof Short) {
				return ((Double) x).doubleValue() / ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Double) x).doubleValue() / ((Byte) y).byteValue();
			}
		} else if (x instanceof BigInteger) {
			if (y instanceof Integer) {
				return ((BigInteger) x).divide(new BigInteger(((Integer) y).toString()));
			} else if (y instanceof Long) {
				return ((BigInteger) x).divide(new BigInteger(((Long) y).toString()));
			} else if (y instanceof Float) {
				return (new BigDecimal((BigInteger) x)).divide(new BigDecimal(((Float) y).toString()), scale, roundingMode);
			} else if (y instanceof Double) {
				return (new BigDecimal((BigInteger) x)).divide(new BigDecimal(((Double) y).toString()), scale, roundingMode);
			} else if (y instanceof BigInteger) {
				return ((BigInteger) x).divide((BigInteger) y);
			} else if (y instanceof BigDecimal) {
				return new BigDecimal((BigInteger) x).divide((BigDecimal) y, scale, roundingMode);
			} else if (y instanceof Short) {
				return ((BigInteger) x).divide(new BigInteger(((Short) y).toString()));
			} else if (y instanceof Byte) {
				return ((BigInteger) x).divide(new BigInteger(((Byte) y).toString()));
			}
		} else if (x instanceof BigDecimal) {
			if (y instanceof Integer) {
				return ((BigDecimal) x).divide(new BigDecimal(((Integer) y).toString()), scale, roundingMode);
			} else if (y instanceof Long) {
				return ((BigDecimal) x).divide(new BigDecimal(((Long) y).toString()), scale, roundingMode);
			} else if (y instanceof Float) {
				return ((BigDecimal) x).divide(new BigDecimal(((Float) y).toString()), scale, roundingMode);
			} else if (y instanceof Double) {
				return ((BigDecimal) x).divide(new BigDecimal(((Double) y).toString()), scale, roundingMode);
			} else if (y instanceof BigInteger) {
				return ((BigDecimal) x).divide(new BigDecimal((BigInteger) y), scale, roundingMode);
			} else if (y instanceof BigDecimal) {
				return ((BigDecimal) x).divide((BigDecimal) y, scale, roundingMode);
			} else if (y instanceof Short) {
				return ((BigDecimal) x).divide(new BigDecimal(((Short) y).toString()), scale, roundingMode);
			} else if (y instanceof Byte) {
				return ((BigDecimal) x).divide(new BigDecimal(((Byte) y).toString()), scale, roundingMode);
			}
		} else if (x instanceof Short) {
			if (y instanceof Integer) {
				return ((Short) x).shortValue() / ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Short) x).shortValue() / ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Short) x).shortValue() / ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Short) x).shortValue() / ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Short) x).toString()).divide((BigInteger) y);
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Short) x).toString()).divide((BigDecimal) y, scale, roundingMode);
			} else if (y instanceof Short) {
				return ((Short) x).shortValue() / ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Short) x).shortValue() / ((Byte) y).byteValue();
			}
		} else if (x instanceof Byte) {
			if (y instanceof Integer) {
				return ((Byte) x).byteValue() / ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Byte) x).byteValue() / ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Byte) x).byteValue() / ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Byte) x).byteValue() / ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Byte) x).toString()).divide((BigInteger) y);
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Byte) x).toString()).divide((BigDecimal) y, scale, roundingMode);
			} else if (y instanceof Short) {
				return ((Byte) x).byteValue() / ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Byte) x).byteValue() / ((Byte) y).byteValue();
			}
		}
		throw new OgnlException("Unsupported operation expression(divide) object. x:" + x.getClass().getName() + ", y:" + y.getClass().getName());

	}

	public static Number remainder(Number x, Number y) {
		if (x instanceof Integer) {
			if (y instanceof Integer) {
				return ((Integer) x).intValue() % ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Integer) x).intValue() % ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Integer) x).intValue() % ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Integer) x).intValue() % ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Integer) x).toString()).remainder((BigInteger) y);
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Integer) x).toString()).remainder((BigDecimal) y);
			} else if (y instanceof Short) {
				return ((Integer) x).intValue() % ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Integer) x).intValue() % ((Byte) y).byteValue();
			}
		} else if (x instanceof Long) {
			if (y instanceof Integer) {
				return ((Long) x).longValue() % ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Long) x).longValue() % ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Long) x).longValue() % ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Long) x).longValue() % ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Long) x).toString()).remainder((BigInteger) y);
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Long) x).toString()).remainder((BigDecimal) y);
			} else if (y instanceof Short) {
				return ((Long) x).longValue() % ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Long) x).longValue() % ((Byte) y).byteValue();
			}
		} else if (x instanceof Float) {
			if (y instanceof Integer) {
				return ((Float) x).floatValue() % ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Float) x).floatValue() % ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Float) x).floatValue() % ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Float) x).floatValue() % ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigDecimal(((Float) x).toString()).remainder(new BigDecimal((BigInteger) y));
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Float) x).toString()).remainder((BigDecimal) y);
			} else if (y instanceof Short) {
				return ((Float) x).floatValue() % ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Float) x).floatValue() % ((Byte) y).byteValue();
			}
		} else if (x instanceof Double) {
			if (y instanceof Integer) {
				return ((Double) x).doubleValue() % ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Double) x).doubleValue() % ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Double) x).doubleValue() % ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Double) x).doubleValue() % ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigDecimal(((Double) x).toString()).remainder(new BigDecimal((BigInteger) y));
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Double) x).toString()).remainder((BigDecimal) y);
			} else if (y instanceof Short) {
				return ((Double) x).doubleValue() % ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Double) x).doubleValue() % ((Byte) y).byteValue();
			}
		} else if (x instanceof BigInteger) {
			if (y instanceof Integer) {
				return ((BigInteger) x).remainder(new BigInteger(((Integer) y).toString()));
			} else if (y instanceof Long) {
				return ((BigInteger) x).remainder(new BigInteger(((Long) y).toString()));
			} else if (y instanceof Float) {
				return (new BigDecimal((BigInteger) x)).remainder(new BigDecimal(((Float) y).toString()));
			} else if (y instanceof Double) {
				return (new BigDecimal((BigInteger) x)).remainder(new BigDecimal(((Double) y).toString()));
			} else if (y instanceof BigInteger) {
				return ((BigInteger) x).remainder((BigInteger) y);
			} else if (y instanceof BigDecimal) {
				return new BigDecimal((BigInteger) x).remainder((BigDecimal) y);
			} else if (y instanceof Short) {
				return ((BigInteger) x).remainder(new BigInteger(((Short) y).toString()));
			} else if (y instanceof Byte) {
				return ((BigInteger) x).remainder(new BigInteger(((Byte) y).toString()));
			}
		} else if (x instanceof BigDecimal) {
			if (y instanceof Integer) {
				return ((BigDecimal) x).remainder(new BigDecimal(((Integer) y).toString()));
			} else if (y instanceof Long) {
				return ((BigDecimal) x).remainder(new BigDecimal(((Long) y).toString()));
			} else if (y instanceof Float) {
				return ((BigDecimal) x).remainder(new BigDecimal(((Float) y).toString()));
			} else if (y instanceof Double) {
				return ((BigDecimal) x).remainder(new BigDecimal(((Double) y).toString()));
			} else if (y instanceof BigInteger) {
				return ((BigDecimal) x).remainder(new BigDecimal((BigInteger) y));
			} else if (y instanceof BigDecimal) {
				return ((BigDecimal) x).remainder((BigDecimal) y);
			} else if (y instanceof Short) {
				return ((BigDecimal) x).remainder(new BigDecimal(((Short) y).toString()));
			} else if (y instanceof Byte) {
				return ((BigDecimal) x).remainder(new BigDecimal(((Byte) y).toString()));
			}
		} else if (x instanceof Short) {
			if (y instanceof Integer) {
				return ((Short) x).shortValue() % ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Short) x).shortValue() % ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Short) x).shortValue() % ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Short) x).shortValue() % ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Short) x).toString()).remainder((BigInteger) y);
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Short) x).toString()).remainder((BigDecimal) y);
			} else if (y instanceof Short) {
				return ((Short) x).shortValue() % ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Short) x).shortValue() % ((Byte) y).byteValue();
			}
		} else if (x instanceof Byte) {
			if (y instanceof Integer) {
				return ((Byte) x).byteValue() % ((Integer) y).intValue();
			} else if (y instanceof Long) {
				return ((Byte) x).byteValue() % ((Long) y).longValue();
			} else if (y instanceof Float) {
				return ((Byte) x).byteValue() % ((Float) y).floatValue();
			} else if (y instanceof Double) {
				return ((Byte) x).byteValue() % ((Double) y).doubleValue();
			} else if (y instanceof BigInteger) {
				return new BigInteger(((Byte) x).toString()).remainder((BigInteger) y);
			} else if (y instanceof BigDecimal) {
				return new BigDecimal(((Byte) x).toString()).remainder((BigDecimal) y);
			} else if (y instanceof Short) {
				return ((Byte) x).byteValue() % ((Short) y).shortValue();
			} else if (y instanceof Byte) {
				return ((Byte) x).byteValue() % ((Byte) y).byteValue();
			}
		}
		throw new OgnlException("Unsupported operation expression(remainder) object. x:" + x.getClass().getName() + ", y:" + y.getClass().getName());
	}

	public static void main(String[] args) {
		// Byte, Double, Float, Integer, Long, Short, BigDecimal, BigInteger
		Number[] xx = { 0x02, 1.4D, 1.3F, 5, 2L, new Short("3"), new BigDecimal(3.4d), new BigInteger("2") };
		// Number[] xx = { 2, 2D, 2F, 2, 2L, new Short("2"), new BigDecimal(2), new BigInteger("2") };
		for (int i = 0; i < xx.length; i++) {
			for (int j = 0; j < xx.length; j++) {
				System.out.println(xx[i] + "+" + xx[j] + ":\t" + add(xx[i], xx[j]));
				System.out.println(xx[i] + "-" + xx[j] + ":\t" + minus(xx[i], xx[j]));
				System.out.println(xx[i] + "*" + xx[j] + ":\t" + multiply(xx[i], xx[j]));
				System.out.println(xx[i] + "/" + xx[j] + ":\t" + divide(xx[i], xx[j]));
				System.out.println(xx[i] + "%" + xx[j] + ":\t" + remainder(xx[i], xx[j]));
			}
			System.out.println();
		}
	}
}
