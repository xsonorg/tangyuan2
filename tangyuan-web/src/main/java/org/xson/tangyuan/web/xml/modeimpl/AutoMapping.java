package org.xson.tangyuan.web.xml.modeimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.web.WebComponent;
import org.xson.tangyuan.web.xml.BuilderContext;
import org.xson.tangyuan.web.xml.ControllerBuilder;
import org.xson.tangyuan.web.xml.ControllerVo;
import org.xson.tangyuan.web.xml.ControllerVo.DataConvertEnum;
import org.xson.tangyuan.web.xml.InterceptVo;
import org.xson.tangyuan.web.xml.InterceptVo.InterceptType;
import org.xson.tangyuan.web.xml.MethodObject;
import org.xson.tangyuan.xml.XmlParseException;

public class AutoMapping extends ControllerBuilder {

	private Log log = LogFactory.getLog(getClass());

	public AutoMapping(BuilderContext bc) {
		this.bc = bc;
	}

	/** 解析控制器,自动映射模式 */
	public void build() {
		Set<String> serviceKeys = TangYuanContainer.getInstance().getServicesKeySet();
		for (String key : serviceKeys) {
			String url = serviceNameToUrl(key);
			if (this.bc.getControllerMap().containsKey(url)) {
				throw new XmlParseException("Duplicate URL: " + url);
			}

			String transfer = key;
			String validate = null;
			MethodObject execMethod = null;
			String permission = null;
			CacheUseVo cacheUse = null;
			// DataConvertEnum convert = DataConvertEnum.BODY;
			DataConvertEnum convert = null;
			// boolean convertByRule = false;
			boolean cacheInAop = WebComponent.getInstance().isCacheInAop();

			List<InterceptVo> assemblyList = new ArrayList<InterceptVo>();
			List<InterceptVo> beforeList = new ArrayList<InterceptVo>();
			List<InterceptVo> afterList = new ArrayList<InterceptVo>();

			ControllerVo cVo = new ControllerVo(url, transfer, validate, execMethod, getInterceptList(url, assemblyList, InterceptType.ASSEMBLY),
					getInterceptList(url, beforeList, InterceptType.BEFORE), getInterceptList(url, afterList, InterceptType.AFTER), permission,
					cacheUse, convert, cacheInAop);

			this.bc.getControllerMap().put(cVo.getUrl(), cVo);
			log.info("Add auto <c> :" + cVo.getUrl());
		}
	}
}
