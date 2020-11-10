package org.xson.tangyuan.cache.apply;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.xson.common.object.XCO;
import org.xson.tangyuan.cache.CacheComponent;
import org.xson.tangyuan.cache.CacheException;
import org.xson.tangyuan.cache.CacheKeyBuilder;
import org.xson.tangyuan.cache.TangYuanCache;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.ognl.vars.warper.GAParserWarper;
import org.xson.tangyuan.util.PatternMatchUtils;
import org.xson.tangyuan.util.StringUtils;

import com.alibaba.fastjson.JSONObject;

public class CacheBase {

	private static final int    CACHE_KEY_ITEM_TYPE_CONSTANT = 1;
	private static final int    CACHE_KEY_ITEM_TYPE_SERVICE  = 2;
	private static final int    CACHE_KEY_ITEM_TYPE_ARG      = 3;
	private static final int    CACHE_KEY_ITEM_TYPE_VARIABLE = 4;

	private static final String KEY_REF_SERVICE1             = "service";
	private static final String KEY_REF_SERVICE2             = "url";
	private static final String KEY_REF_ARG1                 = "arg";
	private static final String KEY_REF_ARG2                 = "args";

	protected TangYuanCache     cache                        = null;
	protected String            simpleKey                    = null;
	protected List<CacheKey>    keyList                      = null;
	protected boolean           encodeKey                    = true;

	class CacheKeyArgFilterItem {
		boolean simple;
		String  context;

		CacheKeyArgFilterItem(boolean simple, String context) {
			this.simple = simple;
			this.context = context;
		}
	}

	class CacheKey {
		// 1: 常量, 2: ${service}, 3:${args}, 4:参数变量
		int                     type;
		// String name;
		Object                  name;
		// ${args[a,b*,c,d]}
		CacheKeyArgFilterItem[] include;
		// ${args[^a,b,c,d]}
		CacheKeyArgFilterItem[] exclude;

		CacheKey(int type, Object name) {
			this.type = type;
			this.name = name;
		}

		CacheKey(int type, Object name, CacheKeyArgFilterItem[] include, CacheKeyArgFilterItem[] exclude) {
			this(type, name);
			this.include = include;
			this.exclude = exclude;
		}
	}

	protected CacheBase(TangYuanCache cache) {
		this.cache = cache;
	}

	/**
	 * 分析key: 原始数据
	 */
	protected void parseKey(String service, String key) {
		List<CacheKey> keyList  = new ArrayList<CacheKey>();
		StringBuilder  builder  = new StringBuilder();
		boolean        simple   = true;
		char[]         src      = key.toCharArray();
		int            position = -1;
		for (int i = 0; i < src.length; i++) {
			char k = src[i];
			switch (k) {
			case '$':
				if ((i + 1) < src.length && '{' == src[i + 1] && (position = findPosition(i + 1, src, '}')) > -1) {
					if (builder.length() > 0) {
						keyList.add(new CacheKey(CACHE_KEY_ITEM_TYPE_CONSTANT, builder.toString()));
						builder = new StringBuilder();
					}
					// 找到${}
					String str = new String(src, i + 2, position - i - 2);
					// 新增功能
					if (KEY_REF_SERVICE1.equalsIgnoreCase(str) || KEY_REF_SERVICE2.equalsIgnoreCase(str)) {
						keyList.add(new CacheKey(CACHE_KEY_ITEM_TYPE_SERVICE, service));
					} else if (KEY_REF_ARG1.equalsIgnoreCase(str) || KEY_REF_ARG2.equalsIgnoreCase(str)) {
						keyList.add(new CacheKey(CACHE_KEY_ITEM_TYPE_ARG, null));
						simple = false;
					} else {
						String                  context          = str.trim();
						String                  contextLowerCase = context.toLowerCase();
						CacheKeyArgFilterItem[] include          = null;
						CacheKeyArgFilterItem[] exclude          = null;
						if (contextLowerCase.startsWith(KEY_REF_ARG1 + "[") && contextLowerCase.endsWith("]")) {
							include = getIncludeArray(context, (KEY_REF_ARG1 + "[").length());
							exclude = getExcludeArray(context, (KEY_REF_ARG1 + "[").length());
						} else if (contextLowerCase.startsWith(KEY_REF_ARG2) && contextLowerCase.endsWith("]")) {
							include = getIncludeArray(context, (KEY_REF_ARG2 + "[").length());
							exclude = getExcludeArray(context, (KEY_REF_ARG2 + "[").length());
						} else {
							throw new CacheException("Illegal expression tag: ${" + str + "}");
						}
						keyList.add(new CacheKey(CACHE_KEY_ITEM_TYPE_ARG, null, include, exclude));
						simple = false;
					}
					// i = position + 1;
					i = position;// fix bug --> '}'
				} else {
					builder.append(k);
				}
				break;
			case '{':
				if ((i + 1) < src.length && (position = findPosition(i + 1, src, '}')) > -1) {
					if (builder.length() > 0) {
						keyList.add(new CacheKey(CACHE_KEY_ITEM_TYPE_CONSTANT, builder.toString()));
						builder = new StringBuilder();
					}
					// 找到{xxx}
					String str = new String(src, i + 1, position - i - 1);
					//					keyList.add(new CacheKey(CACHE_KEY_ITEM_TYPE_VARIABLE, new DefaultValueParserWarper().parse(str)));
					keyList.add(new CacheKey(CACHE_KEY_ITEM_TYPE_VARIABLE, new GAParserWarper().parse(str)));
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

	private CacheKeyArgFilterItem[] getIncludeArray(String context, int start) {
		if ('^' == context.charAt(start)) {
			return null;
		}
		String   filterContext = context.substring(start, context.length() - 1);
		String[] array         = StringUtils.safeSplit(filterContext);
		if (null == array) {
			return null;
		}
		CacheKeyArgFilterItem[] items = new CacheKeyArgFilterItem[array.length];
		for (int i = 0; i < array.length; i++) {
			boolean simple = array[i].indexOf("*") > -1 ? true : false;
			items[i] = new CacheKeyArgFilterItem(simple, array[i]);
		}
		return items;
	}

	private CacheKeyArgFilterItem[] getExcludeArray(String context, int start) {
		if ('^' != context.charAt(start)) {
			return null;
		}
		start = start + 1;
		String   filterContext = context.substring(start, context.length() - 1);
		String[] array         = StringUtils.safeSplit(filterContext);
		if (null == array) {
			return null;
		}
		CacheKeyArgFilterItem[] items = new CacheKeyArgFilterItem[array.length];
		for (int i = 0; i < array.length; i++) {
			boolean simple = array[i].indexOf("*") > -1 ? true : false;
			items[i] = new CacheKeyArgFilterItem(simple, array[i]);
		}
		return items;
	}

	public String buildKey(Object obj) {
		String cacheKey = buildKey0(obj);

		System.out.println("cacheKey:\t" + cacheKey);

		if (encodeKey) {
			CacheKeyBuilder builder = CacheComponent.getInstance().getCacheKeyBuilder();
			cacheKey = builder.build(cacheKey);
			System.out.println("cacheKey:\t" + cacheKey);
		}

		return cacheKey;
	}

	private String buildKey0(Object obj) {
		if (null != simpleKey) {
			return simpleKey;
		} else {
			StringBuilder builder = new StringBuilder();
			for (CacheKey cacheKey : keyList) {
				if (CACHE_KEY_ITEM_TYPE_CONSTANT == cacheKey.type || CACHE_KEY_ITEM_TYPE_SERVICE == cacheKey.type) {
					builder.append(cacheKey.name);
				} else if (CACHE_KEY_ITEM_TYPE_ARG == cacheKey.type) {

					//					if (null == obj) {
					//						builder.append("null");// MD5
					//					} else if (obj instanceof XCO) {
					//						builder.append(MD5(((XCO) obj).toString()));
					//					} else if (obj instanceof Map) {
					//						builder.append(MD5(JSON.toJSONString((Map) obj)));
					//					} else if (obj instanceof JSONObject) {
					//						builder.append(MD5(((JSONObject) obj).toJSONString()));
					//					} else {
					//						throw new CacheException("When building cache.key[3], unsupported parameter type: " + obj.getClass());
					//					}

					if (null == obj) {
						builder.append("null");// MD5
					} else if (obj instanceof XCO) {
						builder.append(argToString((XCO) obj, cacheKey));
					} else if (obj instanceof JSONObject) {
						builder.append(argToString((JSONObject) obj, cacheKey));
					} else {
						throw new CacheException("When building cache.key[3], unsupported parameter type: " + obj.getClass());
					}

				} else if (CACHE_KEY_ITEM_TYPE_VARIABLE == cacheKey.type) {
					if (null == obj) {
						throw new CacheException("When building cache.key[4], the argument can not be empty.");
					} else if (obj instanceof XCO) {
						builder.append(((Variable) cacheKey.name).getValue(obj));
					}
					//					else if (obj instanceof Map) {
					//						builder.append(((Variable) cacheKey.name).getValue(obj));
					//					} 
					else if (obj instanceof JSONObject) {
						builder.append(((JSONObject) obj).get(((Variable) cacheKey.name).getOriginal()));
					} else {
						throw new CacheException("When building cache.key[4], unsupported parameter type: " + obj.getClass());
					}
				}
			}
			return builder.toString();
		}
	}

	private boolean isMatchField(String key, CacheKeyArgFilterItem[] itemArray) {
		for (CacheKeyArgFilterItem item : itemArray) {
			if (item.simple) {
				if (item.context.equals(key)) {
					return true;
				}
			}
			if (PatternMatchUtils.simpleMatch(item.context, key)) {
				return true;
			}
		}
		return false;
	}

	private Set<String> sort(Set<String> set) {
		TreeSet<String> ts = new TreeSet<String>(set);
		return ts;
	}

	private String argToString(XCO arg, CacheKey cacheKey) {
		if (null != cacheKey.exclude) {
			XCO         xco  = new XCO();
			Set<String> keys = sort(arg.keys());
			//			keys.stream().sorted(Comparator.reverseOrder());
			for (String key : keys) {
				boolean match = isMatchField(key, cacheKey.exclude);
				if (!match) {
					xco.setObjectValue(key, arg.get(key));
				}
			}
			arg = xco;
		} else if (null != cacheKey.include) {
			XCO         xco  = new XCO();
			Set<String> keys = sort(arg.keys());
			//			keys.stream().sorted(Comparator.reverseOrder());
			for (String key : keys) {
				boolean match = isMatchField(key, cacheKey.include);
				if (match) {
					xco.setObjectValue(key, arg.get(key));
				}
			}
			arg = xco;
		} else {
			XCO         xco  = new XCO();
			Set<String> keys = sort(arg.keys());
			for (String key : keys) {
				xco.setObjectValue(key, arg.get(key));
			}
			arg = xco;
		}
		return arg.toString();
	}

	private String argToString(JSONObject arg, CacheKey cacheKey) {
		if (null != cacheKey.exclude) {
			JSONObject  obj  = new JSONObject();
			//			Set<String> keys = arg.keySet();
			//			keys.stream().sorted(Comparator.reverseOrder());
			Set<String> keys = sort(arg.keySet());
			for (String key : keys) {
				boolean match = isMatchField(key, cacheKey.exclude);
				if (!match) {
					obj.put(key, arg.get(key));
				}
			}
			arg = obj;
		} else if (null != cacheKey.include) {
			JSONObject  obj  = new JSONObject();
			Set<String> keys = sort(arg.keySet());
			for (String key : keys) {
				boolean match = isMatchField(key, cacheKey.include);
				if (match) {
					obj.put(key, arg.get(key));
				}
			}
			arg = obj;
		} else {
			JSONObject  obj  = new JSONObject();
			Set<String> keys = sort(arg.keySet());
			for (String key : keys) {
				obj.put(key, arg.get(key));
			}
			arg = obj;
		}
		//		builder.append(MD5(((JSONObject) obj).toJSONString()));
		return arg.toJSONString();
	}

	// 异步处理。对于put和clean
	//	protected boolean        asyn= true;
	// <... cache="id:cache01; key:${arg[^$$*]}; expiry:10; mode=sync/asyn" />

	//	private char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
	//
	//	private String MD5(String s) {
	//		try {
	//			// fix bug
	//			//			return MD5(s.getBytes("UTF-8"));
	//			return MD5(s.getBytes(StandardCharsets.UTF_8));
	//		} catch (Throwable e) {
	//			throw new CacheException("Unsupported Encoding Exception: " + s, e);
	//		}
	//	}
	//
	//	private String MD5(byte[] btInput) {
	//		try {
	//			MessageDigest mdInst = MessageDigest.getInstance("MD5");
	//			mdInst.update(btInput);
	//			byte[] md    = mdInst.digest();
	//			int    j     = md.length;
	//			char   str[] = new char[j * 2];
	//			int    k     = 0;
	//			for (int i = 0; i < j; i++) {
	//				byte byte0 = md[i];
	//				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
	//				str[k++] = hexDigits[byte0 & 0xf];
	//			}
	//			return new String(str).substring(8, 24);// 16位
	//		} catch (Throwable e) {
	//			// throw new CacheException("MD5处理失败", e);
	//			throw new CacheException("MD5 Encoding Exception.", e);
	//		}
	//	}

	//	public static void main(String[] args) {
	//		String context       = "arg[a, b]";
	//		int    start         = "arg[".length();
	//		String filterContext = context.substring(start, context.length() - 1);
	//		System.out.println(filterContext);
	//	}

	//		if (null != simpleKey) {
	//			return simpleKey;
	//		} else {
	//			StringBuilder builder = new StringBuilder();
	//			for (CacheKey cacheKey : keyList) {
	//				if (CACHE_KEY_ITEM_TYPE_CONSTANT == cacheKey.type || CACHE_KEY_ITEM_TYPE_SERVICE == cacheKey.type) {
	//					builder.append(cacheKey.name);
	//				} else if (CACHE_KEY_ITEM_TYPE_ARG == cacheKey.type) {
	//
	//					//					if (null == obj) {
	//					//						builder.append("null");// MD5
	//					//					} else if (obj instanceof XCO) {
	//					//						builder.append(MD5(((XCO) obj).toString()));
	//					//					} else if (obj instanceof Map) {
	//					//						builder.append(MD5(JSON.toJSONString((Map) obj)));
	//					//					} else if (obj instanceof JSONObject) {
	//					//						builder.append(MD5(((JSONObject) obj).toJSONString()));
	//					//					} else {
	//					//						throw new CacheException("When building cache.key[3], unsupported parameter type: " + obj.getClass());
	//					//					}
	//
	//					if (null == obj) {
	//						builder.append("null");// MD5
	//					} else if (obj instanceof XCO) {
	//						builder.append(argToString((XCO) obj, cacheKey));
	//					} else {
	//						throw new CacheException("When building cache.key[3], unsupported parameter type: " + obj.getClass());
	//					}
	//
	//				} else if (CACHE_KEY_ITEM_TYPE_VARIABLE == cacheKey.type) {
	//					if (null == obj) {
	//						throw new CacheException("When building cache.key[4], the argument can not be empty.");
	//					} else if (obj instanceof XCO) {
	//						builder.append(((Variable) cacheKey.name).getValue(obj));
	//					} else if (obj instanceof Map) {
	//						builder.append(((Variable) cacheKey.name).getValue(obj));
	//					} else if (obj instanceof JSONObject) {
	//						builder.append(((JSONObject) obj).get(((Variable) cacheKey.name).getOriginal()));
	//					} else {
	//						throw new CacheException("When building cache.key[4], unsupported parameter type: " + obj.getClass());
	//					}
	//				}
	//			}
	//			return builder.toString();
	//		}
}
