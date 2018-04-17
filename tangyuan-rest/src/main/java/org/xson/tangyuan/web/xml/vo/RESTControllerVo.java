package org.xson.tangyuan.web.xml.vo;

import java.util.List;

import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.web.DataConverter;
import org.xson.tangyuan.web.ResponseHandler;
import org.xson.tangyuan.web.RequestContext.RequestTypeEnum;
import org.xson.tangyuan.web.rest.RestURIVo;

public class RESTControllerVo extends ControllerVo {

	private String		originalURI;	// "/a/{id}/b?x=1"
	private RestURIVo	restURIVo;

	public RESTControllerVo(String originalURI, String url, RequestTypeEnum requestType, String transfer, String validate, MethodObject execMethod,
			List<MethodObject> assemblyMethods, List<MethodObject> beforeMethods, List<MethodObject> afterMethods, String permission,
			CacheUseVo cacheUse, DataConverter dataConverter, boolean cacheInAop, ResponseHandler responseHandler, RestURIVo restURIVo) {
		super(url, requestType, transfer, validate, execMethod, assemblyMethods, beforeMethods, afterMethods, permission, cacheUse, dataConverter,
				cacheInAop, responseHandler);
		this.originalURI = originalURI;
		this.restURIVo = restURIVo;
	}

	public RestURIVo getRestURIVo() {
		return restURIVo;
	}

	public String getOriginalURI() {
		return originalURI;
	}
}
