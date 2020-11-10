package org.xson.tangyuan.web.convert;

import java.util.List;

import org.xson.tangyuan.web.DataConverter;
import org.xson.tangyuan.web.RequestContext;

public class MixedDataConverter implements DataConverter {

	private List<DataConverter> converters;

	public MixedDataConverter(List<DataConverter> converters) {
		this.converters = converters;
	}

	@Override
	public void convert(RequestContext requestContext) throws Throwable {
		for (DataConverter converter : converters) {
			converter.convert(requestContext);
		}
	}

	//	@Override
	//	public void convert(RequestContext requestContext, ControllerVo cVo) throws Throwable {
	//		for (DataConverter converter : converters) {
	//			converter.convert(requestContext, cVo);
	//		}
	//	}

}
