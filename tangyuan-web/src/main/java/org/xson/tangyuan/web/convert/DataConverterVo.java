package org.xson.tangyuan.web.convert;

import java.util.List;

import org.xson.tangyuan.util.PatternMatchUtils;
import org.xson.tangyuan.web.DataConverter;

public class DataConverterVo {

	private DataConverter converter;
	private List<String>  includeList;
	private List<String>  excludeList;

	public DataConverterVo(DataConverter converter, List<String> includeList, List<String> excludeList) {
		this.converter = converter;
		this.includeList = includeList;
		this.excludeList = excludeList;
	}

	public boolean match(String url) {
		if (null != excludeList) {
			for (String pattern : excludeList) {
				if (PatternMatchUtils.simpleMatch(pattern, url)) {
					return false;
				}
			}
		}
		// fix bug
		if (null != includeList) {
			for (String pattern : includeList) {
				if (PatternMatchUtils.simpleMatch(pattern, url)) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

	public DataConverter getConverter() {
		return converter;
	}
}
