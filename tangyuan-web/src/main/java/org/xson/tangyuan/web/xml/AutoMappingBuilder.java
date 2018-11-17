package org.xson.tangyuan.web.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.web.DataConverter;
import org.xson.tangyuan.web.RequestContext.RequestTypeEnum;
import org.xson.tangyuan.web.WebComponent;
import org.xson.tangyuan.web.xml.vo.ControllerVo;
import org.xson.tangyuan.web.xml.vo.InterceptVo;
import org.xson.tangyuan.web.xml.vo.InterceptVo.InterceptType;
import org.xson.tangyuan.web.xml.vo.MethodObject;
import org.xson.tangyuan.xml.XmlParseException;

public class AutoMappingBuilder extends ControllerBuilder {

	private Log log = LogFactory.getLog(getClass());

	public AutoMappingBuilder(XMLWebContext context) {
		this.context = context;
	}

	/** 解析控制器,自动映射模式 */
	public void build() {
		Set<String> serviceKeys = TangYuanContainer.getInstance().getServicesKeySet();
		for (String key : serviceKeys) {
			String url = serviceNameToUrl(key);
			if (this.context.getControllerMap().containsKey(url)) {
				throw new XmlParseException("Duplicate URL: " + url);
			}

			RequestTypeEnum requestType = null;
			String transfer = key;
			String validate = null;
			MethodObject execMethod = null;
			String permission = null;
			CacheUseVo cacheUse = null;
			DataConverter convert = null;
			boolean cacheInAop = WebComponent.getInstance().isCacheInAop();

			List<InterceptVo> assemblyList = new ArrayList<InterceptVo>();
			List<InterceptVo> beforeList = new ArrayList<InterceptVo>();
			List<InterceptVo> afterList = new ArrayList<InterceptVo>();

			ControllerVo cVo = new ControllerVo(url, requestType, transfer, validate, execMethod,
					getInterceptList(url, assemblyList, InterceptType.ASSEMBLY), getInterceptList(url, beforeList, InterceptType.BEFORE),
					getInterceptList(url, afterList, InterceptType.AFTER), permission, cacheUse, convert, cacheInAop, null);

			this.context.getControllerMap().put(cVo.getUrl(), cVo);
			log.info("Add auto <c> :" + cVo.getUrl());
		}
	}
}
