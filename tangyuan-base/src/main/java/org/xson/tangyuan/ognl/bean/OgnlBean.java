package org.xson.tangyuan.ognl.bean;

import java.util.Map;
import java.util.Map.Entry;

import org.xson.common.object.XCO;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.ognl.FieldVo;
import org.xson.tangyuan.ognl.FieldVoWrapper;
import org.xson.tangyuan.ognl.OgnlException;
import org.xson.tangyuan.util.TypeUtils;

public class OgnlBean {

	private static Log log = LogFactory.getLog(OgnlBean.class);

	/**
	 * 从数据库容中的行记录转换成Bean
	 * 
	 * @param data
	 *            行记录
	 * @param clazz
	 * @return
	 */
	public static Object mapToBean(Map<String, Object> data, Class<?> clazz) {
		try {
			FieldVoWrapper fieldVoWrapper = TypeUtils.getBeanField(clazz);
			Map<String, FieldVo> fieldMap = fieldVoWrapper.getFieldMap();
			Object instance = clazz.newInstance();
			for (Entry<String, Object> entry : data.entrySet()) {
				FieldVo fieldVo = fieldMap.get(entry.getKey());
				if (null != fieldVo) {
					Object value = entry.getValue();
					if (null != value) {
						fieldVo.getSetter().invoke(instance, value);
					}
				} else {
					log.error("没有匹配的FieldVo: " + entry.getKey() + ", class:" + clazz);
				}
			}
			return instance;
		} catch (Exception e) {
			throw new OgnlException(e);
		}
	}

	/**
	 * XCO to bean (简单结构)
	 */
	public static Object xcoToBean(XCO data, Class<?> clazz) {
		try {
			FieldVoWrapper fieldVoWrapper = TypeUtils.getBeanField(clazz);
			Map<String, FieldVo> fieldMap = fieldVoWrapper.getFieldMap();
			Object instance = clazz.newInstance();
			for (Entry<String, FieldVo> entry : fieldMap.entrySet()) {
				Object value = data.getObjectValue(entry.getKey());
				if (null != value) {
					FieldVo fieldVo = entry.getValue();
					fieldVo.getSetter().invoke(instance, value);
				} else {
					log.error("没有匹配的FieldVo: " + entry.getKey() + ", class:" + clazz);
				}
			}
			return instance;
		} catch (Exception e) {
			throw new OgnlException(e);
		}
	}

	/**
	 * 从数据库容中的行记录转换成Bean(暂时用不到)
	 * 
	 * @param data
	 *            行记录
	 * @param clazz
	 * @param mVo
	 *            映射对象
	 * @return
	 */
	protected static Object mapToBean(Map<String, Object> data, Class<?> clazz, MappingVo mVo) {
		try {
			FieldVoWrapper fieldVoWrapper = TypeUtils.getBeanField(clazz);
			Map<String, FieldVo> fieldMap = fieldVoWrapper.getFieldMap();
			Object instance = clazz.newInstance();
			for (Entry<String, Object> entry : data.entrySet()) {
				// 获取数据库中列映射的Bean属性名, 这里有问题, 应该从数据中直接映射
				String property = mVo.getProperty(entry.getKey());
				if (null != property) {
					// TODO 以后需要区分是复杂属性还是简单属性
					FieldVo fieldVo = fieldMap.get(property);
					if (null != fieldVo) {
						Object value = entry.getValue();
						if (null != value) {
							fieldVo.getSetter().invoke(instance, value);
						}
					} else {
						log.error("没有匹配的FieldVo: " + property);
					}
				} else {
					log.error("没有匹配的属性名: " + entry.getKey());
				}
			}
			return instance;
		} catch (Exception e) {
			throw new OgnlException(e);
		}
	}
}
