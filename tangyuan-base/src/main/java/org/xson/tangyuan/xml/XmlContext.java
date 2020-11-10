package org.xson.tangyuan.xml;

import java.util.Map;

import org.xson.tangyuan.mapping.MappingVo;

public interface XmlContext {

	void clean();

	//	TangYuanManager getTangYuanManager();

	Map<String, MappingVo> getMappingVoMap();
}
