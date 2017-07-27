package org.xson.tangyuan.cache.apply;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xson.common.object.XCO;
import org.xson.tangyuan.cache.CacheException;
import org.xson.tangyuan.cache.TangYuanCache;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.ognl.vars.warper.DefaultValueParserWarper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class CacheBase {

	protected TangYuanCache		cache;
	protected String			simpleKey;
	protected List<CacheKey>	keyList;

	// protected String service;
	// protected CacheVo cacheVo;
	// /** 需要忽略的cache id */
	// protected String[] ignore;
	// protected CacheBase(CacheVo cacheVo, String[] ignore, String service) {
	// this.cacheVo = cacheVo;
	// this.ignore = ignore;
	// this.service = service;
	// }

	class CacheKey {
		// 1: 常量, 2: ${service}, 3:${args}, 4:参数变量
		int				type;
		// String name;
		public Object	name;

		CacheKey(int type, Object name) {
			this.type = type;
			this.name = name;
		}
	}

	protected CacheBase(TangYuanCache cache) {
		this.cache = cache;
	}

	/**
	 * 分析key: 原始数据
	 */
	protected void parseKey(String service, String key) {
		List<CacheKey> keyList = new ArrayList<CacheKey>();
		StringBuilder builder = new StringBuilder();
		boolean simple = true;
		char[] src = key.toCharArray();
		int position = -1;
		for (int i = 0; i < src.length; i++) {
			char k = src[i];
			switch (k) {
			case '$':
				if ((i + 1) < src.length && '{' == src[i + 1] && (position = findPosition(i + 1, src, '}')) > -1) {
					if (builder.length() > 0) {
						keyList.add(new CacheKey(1, builder.toString()));
						builder = new StringBuilder();
					}
					// 找到${}
					String str = new String(src, i + 2, position - i - 2);
					// if ("service".equalsIgnoreCase(str)) {
					// keyList.add(new CacheKey(2, service));
					// } else if ("args".equalsIgnoreCase(str)) {
					// keyList.add(new CacheKey(3, null));
					// simple = false;
					// } else {
					// throw new CacheException("不合法的表达式标签: ${" + str + "}");
					// }

					// 新增功能
					if ("service".equalsIgnoreCase(str) || "url".equalsIgnoreCase(str)) {
						keyList.add(new CacheKey(2, service));
					} else if ("args".equalsIgnoreCase(str) || "arg".equalsIgnoreCase(str)) {
						keyList.add(new CacheKey(3, null));
						simple = false;
					} else {
						throw new CacheException("不合法的表达式标签: ${" + str + "}");
					}

					// i = position + 1;
					i = position;// fix bug
				} else {
					builder.append(k);
				}
				break;
			case '{':
				if ((i + 1) < src.length && (position = findPosition(i + 1, src, '}')) > -1) {
					if (builder.length() > 0) {
						keyList.add(new CacheKey(1, builder.toString()));
						builder = new StringBuilder();
					}
					// 找到{}
					String str = new String(src, i + 1, position - i - 1);
					// keyList.add(new CacheKey(4, str));
					// keyList.add(new CacheKey(4, VariableParser.parse(str,
					// false)));
					// keyList.add(new CacheKey(4, VariableParser.parse(str, true)));
					keyList.add(new CacheKey(4, new DefaultValueParserWarper().parse(str)));
					simple = false;
					// i = position + 1;
					i = position;// fix bug
				} else {
					builder.append(k);
				}
				break;
			default:
				builder.append(k);
			}
		}

		if (builder.length() > 0) {
			keyList.add(new CacheKey(1, builder.toString()));
		}

		if (simple) {
			builder = new StringBuilder();
			for (CacheKey ck : keyList) {
				builder.append(ck.name);
			}
			this.simpleKey = builder.toString();
		} else {
			this.keyList = keyList;
		}
	}

	private int findPosition(int index, char[] src, char desc) {
		for (int i = index; i < src.length; i++) {
			if (src[i] == desc) {
				return i;
			}
		}
		return -1;
	}

	@SuppressWarnings("rawtypes")
	protected String buildKey(Object obj) {
		if (null != simpleKey) {
			return simpleKey;
		} else {
			StringBuilder builder = new StringBuilder();
			for (CacheKey cacheKey : keyList) {
				if (1 == cacheKey.type || 2 == cacheKey.type) {
					builder.append(cacheKey.name);
				} else if (3 == cacheKey.type) {
					if (null == obj) {
						builder.append("null");// MD5
					} else if (obj instanceof XCO) {
						builder.append(MD5(((XCO) obj).toString()));
					} else if (obj instanceof Map) {
						builder.append(MD5(JSON.toJSONString((Map) obj)));
					} else if (obj instanceof JSONObject) {
						builder.append(MD5(((JSONObject) obj).toJSONString()));
					} else {
						throw new CacheException("不支持的key参数类型:" + obj.getClass());
					}
				} else if (4 == cacheKey.type) {
					if (null == obj) {
						throw new CacheException("变量模式下参数不能为空");
					} else if (obj instanceof XCO) {
						builder.append(((Variable) cacheKey.name).getValue(obj));
					} else if (obj instanceof Map) {
						builder.append(((Variable) cacheKey.name).getValue(obj));
					} else if (obj instanceof JSONObject) {
						builder.append(((JSONObject) obj).get(((Variable) cacheKey.name).getOriginal()));
					} else {
						throw new CacheException("不支持的key参数类型:" + obj.getClass());
					}
				}
			}
			return builder.toString();
		}
	}

	private char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	private String MD5(String s) {
		return MD5(s.getBytes());
	}

	private String MD5(byte[] btInput) {
		try {
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			mdInst.update(btInput);
			byte[] md = mdInst.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str).substring(8, 24);// 16位
		} catch (Exception e) {
			throw new CacheException("MD5处理失败", e);
		}
	}
}
