package org.xson.tangyuan.mq.vo;

import java.util.List;

public class BindingVo {

	private String					channel;
	private Object					key;
	private List<BindingPattern>	patterns;

	public BindingVo(String channel, Object key, List<BindingPattern> patterns) {
		this.channel = channel;
		this.key = key;
		this.patterns = patterns;
	}

	public Object getKey() {
		return key;
	}

	public String getChannel() {
		return channel;
	}

	public List<BindingPattern> getPatterns() {
		return patterns;
	}

}
