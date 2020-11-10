package org.xson.tangyuan.es.datasource;

import java.util.Map;
import java.util.Map.Entry;

public class EsSourceManager {

	private EsSourceVo              defaultVo     = null;
	private Map<String, EsSourceVo> esSourceVoMap = null;

	public EsSourceManager(EsSourceVo essVo, Map<String, EsSourceVo> voMap) {
		if (null != essVo) {
			this.defaultVo = essVo;
		} else {
			this.esSourceVoMap = voMap;
		}
	}

	public String getDefaultEsKey() {
		if (null != defaultVo) {
			return defaultVo.getId();
		}
		return null;
	}

	public boolean isValidEsKey(String esKey) {
		if (null != defaultVo) {
			return defaultVo.getId().equals(esKey);
		}
		return esSourceVoMap.containsKey(esKey);
	}

	public EsSourceVo getEsSource(String esKey) {
		if (null != defaultVo) {
			return defaultVo;
		}
		return esSourceVoMap.get(esKey);
	}

	public void start() throws Throwable {
		if (null != defaultVo) {
			defaultVo.start();
		} else {
			for (Entry<String, EsSourceVo> entry : esSourceVoMap.entrySet()) {
				entry.getValue().start();
			}
		}
	}

	public void stop() {
		if (null != defaultVo) {
			defaultVo.stop();
		} else {
			for (Entry<String, EsSourceVo> entry : esSourceVoMap.entrySet()) {
				entry.getValue().stop();
			}
		}
	}

	//	public void setEsSourceVoMap(EsSourceVo essVo, Map<String, EsSourceVo> voMap) {
	//		if (null != essVo) {
	//			defaultVo = essVo;
	//		} else {
	//			esSourceVoMap = voMap;
	//		}
	//	}
}
