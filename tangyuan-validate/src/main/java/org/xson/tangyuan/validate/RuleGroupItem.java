package org.xson.tangyuan.validate;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

import org.xson.common.object.XCO;
import org.xson.logging.Log;
import org.xson.logging.LogFactory;

public class RuleGroupItem {

	private static Log	log	= LogFactory.getLog(RuleGroupItem.class);

	private String		fieldName;
	private TypeEnum	type;
	private List<Rule>	rules;
	private boolean		require;
	private Object		defaultValue;
	private String		desc;
	private String		message;
	private int			code;

	public RuleGroupItem(String fieldName, TypeEnum type, List<Rule> rules, boolean require, String defaultValue, String desc, String message,
			int code) {
		this.fieldName = fieldName;
		this.type = type;
		this.rules = rules;
		this.require = require;
		this.desc = desc;
		this.message = message;
		this.code = code;
		parseDefaultValue(defaultValue);
	}

	public boolean check(XCO xco, boolean forcedThrowException) {
		boolean result = false;
		try {
			Object value = xco.getObjectValue(fieldName);
			// 需要做非必填的判断
			if (null == value) {
				if (require) {
					result = false;
				} else {
					if (null != defaultValue) {
						setDefaultValue(xco);
					}
					return true;
				}
			} else {
				if (rules.size() > 0) {
					for (Rule rule : rules) {
						Checker checker = rule.findChecker(type);
						if (null == checker) {
							throw new XCOValidateException("Field type and validation rules do not match: " + fieldName);
						}
						result = checker.check(xco, this.fieldName, rule.getValue());
						if (!result) {
							break;
						}
					}
				} else {
					// 在没有规则的情况, 支持类型的验证
					result = checkValueType(value, xco);
				}
			}
		} catch (Throwable e) {
			log.error(null, e);
		}

		// if (!result && ValidateComponent.getInstance().isThrowException()) {
		// throw new XCOValidateException(this.code, this.message);
		// }

		if (!result) {
			if (forcedThrowException) {
				throw new XCOValidateException(this.code, this.message);
			}
			if (ValidateComponent.getInstance().isThrowException()) {
				throw new XCOValidateException(this.code, this.message);
			}
		}

		return result;
	}

	private boolean checkValueType(Object value, XCO data) {
		if (TypeEnum.INTEGER == type) {
			if (Integer.class == value.getClass()) {
				return true;
			}
		} else if (TypeEnum.LONG == type) {
			if (Long.class == value.getClass()) {
				return true;
			}
		} else if (TypeEnum.FLOAT == type) {
			if (Float.class == value.getClass()) {
				return true;
			}
		} else if (TypeEnum.DOUBLE == type) {
			if (Double.class == value.getClass()) {
				return true;
			}
		} else if (TypeEnum.STRING == type) {
			if (String.class == value.getClass()) {
				return true;
			}
		} else if (TypeEnum.DATE == type) {
			if (java.sql.Date.class == value.getClass()) {
				return true;
			}
		} else if (TypeEnum.TIME == type) {
			if (java.sql.Time.class == value.getClass()) {
				return true;
			}
		} else if (TypeEnum.TIMESTAMP == type) {
			if (java.sql.Time.class == value.getClass()) {
				return true;
			}
		} else if (TypeEnum.DATETIME == type) {
			if (java.util.Date.class == value.getClass()) {
				return true;
			}
		} else if (TypeEnum.BIGINTEGER == type) {
			if (BigInteger.class == value.getClass()) {
				return true;
			}
		} else if (TypeEnum.BIGDECIMAL == type) {
			if (BigDecimal.class == value.getClass()) {
				return true;
			}
		} else if (TypeEnum.ARRAY == type) {
			if (value.getClass().isArray()) {
				return true;
			}
		} else if (TypeEnum.COLLECTION == type) {
			if (value instanceof Collection) {
				return true;
			}
		}

		else if (TypeEnum.XCO == type) {
			if (XCO.class == value.getClass()) {
				return true;
			}
		}

		else if (TypeEnum.STRING_ARRAY == type) {
			if (String[].class == value.getClass()) {
				return true;
			}
		}

		else if (TypeEnum.XCO_ARRAY == type) {
			if (XCO[].class == value.getClass()) {
				return true;
			}
		}

		else if (TypeEnum.STRING_LIST == type) {
			if (null != data.getStringListValue(fieldName)) {
				return true;
			}
		}

		else if (TypeEnum.XCO_LIST == type) {
			if (null != data.getXCOListValue(fieldName)) {
				return true;
			}
		}

		else if (TypeEnum.STRING_SET == type) {
			if (null != data.getStringSetValue(fieldName)) {
				return true;
			}
		}

		else if (TypeEnum.XCO_SET == type) {
			if (null != data.getXCOSetValue(fieldName)) {
				return true;
			}
		}

		else if (TypeEnum.BYTE == type) {
			if (Byte.class == value.getClass()) {
				return true;
			}
		}

		else if (TypeEnum.BOOLEAN == type) {
			if (Boolean.class == value.getClass()) {
				return true;
			}
		}

		else if (TypeEnum.SHORT == type) {
			if (Short.class == value.getClass()) {
				return true;
			}
		}

		else if (TypeEnum.CHAR == type) {
			if (Character.class == value.getClass()) {
				return true;
			}
		}

		else if (TypeEnum.INT_ARRAY == type) {
			if (int[].class == value.getClass()) {
				return true;
			}
		}

		else if (TypeEnum.LONG_ARRAY == type) {
			if (long[].class == value.getClass()) {
				return true;
			}
		}

		else if (TypeEnum.FLOAT_ARRAY == type) {
			if (float[].class == value.getClass()) {
				return true;
			}
		}

		else if (TypeEnum.DOUBLE_ARRAY == type) {
			if (double[].class == value.getClass()) {
				return true;
			}
		}

		else if (TypeEnum.BYTE_ARRAY == type) {
			if (byte[].class == value.getClass()) {
				return true;
			}
		}

		else if (TypeEnum.BOOLEAN_ARRAY == type) {
			if (boolean[].class == value.getClass()) {
				return true;
			}
		}

		else if (TypeEnum.SHORT_ARRAY == type) {
			if (short[].class == value.getClass()) {
				return true;
			}
		}

		else if (TypeEnum.CHAR_ARRAY == type) {
			if (char[].class == value.getClass()) {
				return true;
			}
		}

		return false;
	}

	private void parseDefaultValue(String value) {
		if (null == value) {
			return;
		}
		if (this.type == TypeEnum.INTEGER) {
			this.defaultValue = Integer.parseInt(value);
		} else if (this.type == TypeEnum.LONG) {
			this.defaultValue = Long.parseLong(value);
		} else if (this.type == TypeEnum.FLOAT) {
			this.defaultValue = Float.parseFloat(value);
		} else if (this.type == TypeEnum.DOUBLE) {
			this.defaultValue = Double.parseDouble(value);
		} else if (this.type == TypeEnum.STRING) {
			this.defaultValue = value;
		} else if (this.type == TypeEnum.BIGINTEGER) {
			// this.defaultValue = new BigInteger((String) defaultValue);
			this.defaultValue = new BigInteger(value);
		} else if (this.type == TypeEnum.BIGDECIMAL) {
			// this.defaultValue = new BigDecimal((String) defaultValue);
			this.defaultValue = new BigDecimal(value);
		} else if (this.type == TypeEnum.BYTE) {
			this.defaultValue = Byte.parseByte(value);
		} else if (this.type == TypeEnum.BOOLEAN) {
			this.defaultValue = Boolean.parseBoolean(value);
		} else if (this.type == TypeEnum.SHORT) {
			this.defaultValue = Short.parseShort(value);
		} else if (this.type == TypeEnum.CHAR) {
			this.defaultValue = value.charAt(0);
		}
	}

	private void setDefaultValue(XCO xco) {
		if (this.type == TypeEnum.INTEGER) {
			xco.setIntegerValue(this.fieldName, (Integer) defaultValue);
		} else if (this.type == TypeEnum.LONG) {
			xco.setLongValue(this.fieldName, (Long) defaultValue);
		} else if (this.type == TypeEnum.FLOAT) {
			xco.setFloatValue(this.fieldName, (Float) defaultValue);
		} else if (this.type == TypeEnum.DOUBLE) {
			xco.setDoubleValue(this.fieldName, (Double) defaultValue);
		} else if (this.type == TypeEnum.STRING) {
			xco.setStringValue(this.fieldName, (String) defaultValue);
		} else if (this.type == TypeEnum.BIGINTEGER) {
			xco.setBigIntegerValue(this.fieldName, (BigInteger) defaultValue);
		} else if (this.type == TypeEnum.BIGDECIMAL) {
			xco.setBigDecimalValue(this.fieldName, (BigDecimal) defaultValue);
		} else if (this.type == TypeEnum.BYTE) {
			xco.setByteValue(this.fieldName, (Byte) defaultValue);
		} else if (this.type == TypeEnum.BOOLEAN) {
			xco.setBooleanValue(this.fieldName, (Boolean) defaultValue);
		} else if (this.type == TypeEnum.SHORT) {
			xco.setShortValue(this.fieldName, (Short) defaultValue);
		} else if (this.type == TypeEnum.CHAR) {
			xco.setCharValue(this.fieldName, (Character) defaultValue);
		}
	}

	public String getFieldName() {
		return fieldName;
	}

	public TypeEnum getType() {
		return type;
	}

	public String getDesc() {
		return desc;
	}

	public boolean isRequire() {
		return require;
	}

	public String getMessage() {
		return message;
	}

	public int getCode() {
		return code;
	}

	public List<Rule> getRules() {
		return rules;
	}
}
