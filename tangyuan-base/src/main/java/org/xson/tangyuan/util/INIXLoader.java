package org.xson.tangyuan.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.xson.common.object.XCO;
import org.xson.common.object.XCOUtil;
import org.xson.tangyuan.TangYuanException;

/**
 * inix配置文件读取
 */
public class INIXLoader {

	private String separator = ",";

	public XCO load(InputStream in) throws IOException {
		return load(in, null);
	}

	public XCO load(InputStream in, XCO xco) throws IOException {
		if (null == xco) {
			xco = new XCO();
		}
		List<String> content = getContent(in);
		XCO cur = xco;
		for (String line : content) {
			if (line.startsWith("[") && line.endsWith("]")) {
				String section = line.substring(1, line.length() - 1).trim();
				if (section.indexOf(".") > -1) {
					throw new TangYuanException("invalid character '.' in section[" + section + "] in inix file.");
				}
				if ("".equals(section)) {
					cur = xco;
				} else {
					cur = new XCO();
					xco.setXCOValue(section, cur);
				}
			}
			String[] kv = getKV(line);
			if (null == kv) {
				continue;
			}
			checkKey(kv[0]);
			Object[] ov = parseValue(kv);
			cur.setObjectValue((String) ov[0], ov[1]);
		}
		return xco;
	}

	private String[] getKV(String line) {
		int pos = line.indexOf("=");
		if (pos < 1) {
			return null;
		}
		String k = line.substring(0, pos).trim();
		String v = line.substring(pos + 1, line.length()).trim();
		return new String[] { k, v };
	}

	private Object[] parseValue(String[] kv) {

		String key = kv[0];
		String val = kv[1];

		String name = key;
		Object value = val;

		int pos = key.indexOf(":");
		if (pos > 0) {
			String type = key.substring(0, pos);
			name = key.substring(pos + 1, key.length());

			if ("".equals(val) && !"S".equalsIgnoreCase(type)) {
				throw new TangYuanException("The value of '" + name + "' cannot be empty.");
			}

			if ("B".equalsIgnoreCase(type)) {
				value = Byte.parseByte(val);
			} else if ("H".equalsIgnoreCase(type)) {
				value = Short.parseShort(val);
			} else if ("I".equalsIgnoreCase(type)) {
				value = Integer.parseInt(val);
			} else if ("L".equalsIgnoreCase(type)) {
				value = Long.parseLong(val);
			} else if ("F".equalsIgnoreCase(type)) {
				value = Float.parseFloat(val);
			} else if ("D".equalsIgnoreCase(type)) {
				value = Double.parseDouble(val);
			} else if ("C".equalsIgnoreCase(type)) {
				value = val.charAt(0);
			} else if ("O".equalsIgnoreCase(type)) {
				value = Boolean.parseBoolean(val);
			} else if ("S".equalsIgnoreCase(type)) {
				value = val;
			} else if ("A".equalsIgnoreCase(type)) {
				value = XCOUtil.parseDateTime(val); // A: date
			} else if ("E".equalsIgnoreCase(type)) {
				value = XCOUtil.parseDate(val); // E: sql.date
			} else if ("G".equalsIgnoreCase(type)) {
				value = XCOUtil.parseTime(val); // G: sql.time
			} else if ("J".equalsIgnoreCase(type)) {
				value = XCOUtil.parseTimestamp(val);// J: sql.timestamp
			} else if ("K".equalsIgnoreCase(type)) {
				value = new BigInteger(val);
			} else if ("M".equalsIgnoreCase(type)) {
				value = new BigDecimal(val);
			}

			else if ("SA".equalsIgnoreCase(type)) {
				value = val.split(separator);
			} else if ("IA".equalsIgnoreCase(type)) {
				value = val.split(separator);
				value = toNumberArray((String[]) value, int.class);
			}

			else if ("SL".equalsIgnoreCase(type)) {
				value = val.split(separator);
				value = Arrays.asList(value);
			}

			else {
				throw new TangYuanException("Unknown data type: " + type);
			}
		}
		return new Object[] { name, value };
	}

	private void checkKey(String key) {
		if (key.indexOf(".") > -1) {
			throw new TangYuanException("invalid character '.' in key[" + key + "] in inix file.");
		}
	}

	private List<String> getContent(InputStream in) throws IOException {
		List<String> list = new ArrayList<String>();
		// BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		String line = null;
		while (null != (line = reader.readLine())) {
			line = line.trim();
			if (line.length() > 0 && !line.startsWith("#")) {
				list.add(line);
			}
		}
		reader.close();
		return list;
	}

	private Object toNumberArray(String[] arr, Class<?> type) {
		if (int.class == type) {
			if (null == arr || 0 == arr.length) {
				return new int[0];
			}
			int[] result = new int[arr.length];
			for (int i = 0; i < arr.length; i++) {
				result[i] = Integer.parseInt(arr[i]);
			}
			return result;
		}
		return null;
	}

}
