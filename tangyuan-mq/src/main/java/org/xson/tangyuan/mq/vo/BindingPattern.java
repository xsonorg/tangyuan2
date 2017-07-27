package org.xson.tangyuan.mq.vo;

public class BindingPattern {

	private String	pattern;
	
	private boolean	patternMatch;

	public BindingPattern(String pattern, boolean patternMatch) {
		this.pattern = pattern;
		this.patternMatch = patternMatch;
	}

	public String getPattern() {
		return pattern;
	}

	public boolean isPatternMatch() {
		return patternMatch;
	}

}
