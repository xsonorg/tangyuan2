package org.xson.tangyuan.web.convert;

import java.util.List;

import org.xson.tangyuan.web.DataConverter;
import org.xson.tangyuan.web.RequestContext;
import org.xson.tangyuan.web.xml.vo.ControllerVo;

public class MixedDataConverter implements DataConverter {

	private List<DataConverter> converters;

	public MixedDataConverter(List<DataConverter> converters) {
		this.converters = converters;
	}

	@Override
	public void convert(RequestContext requestContext, ControllerVo cVo) throws Throwable {
		for (DataConverter converter : converters) {
			converter.convert(requestContext, cVo);
		}
	}

}
