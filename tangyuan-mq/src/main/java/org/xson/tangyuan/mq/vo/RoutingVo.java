package org.xson.tangyuan.mq.vo;

public class RoutingVo {

	// <routing channel="q0,q1,q2" key="{x}" pattern="abc*" />

	private String	channel;
	private Object	key;
	private String	pattern;
	private boolean	patternMatch;

	public RoutingVo(String channel, Object key, String pattern, boolean patternMatch) {
		this.channel = channel;
		this.key = key;
		this.pattern = pattern;
		this.patternMatch = patternMatch;
	}

	public String getChannel() {
		return channel;
	}

	public Object getKey() {
		return key;
	}

	public String getPattern() {
		return pattern;
	}

	public boolean isPatternMatch() {
		return patternMatch;
	}

}
