package org.xson.tangyuan.ognl.vars;

/**
 * 变量解析配置类
 */
public class VariableConfig {

	private String			openToken;

	private String			closeToken;

	/**
	 * 是否允许嵌套
	 */
	private boolean			existNested;

	private boolean			allowNested;

	private ParserWarper	warper;

	public VariableConfig(String openToken, String closeToken, ParserWarper warper) {
		this(openToken, closeToken, false, warper);
	}

	public VariableConfig(String openToken, String closeToken, boolean allowNested, ParserWarper warper) {
		this.openToken = openToken;
		this.closeToken = closeToken;
		this.warper = warper;

		this.allowNested = allowNested;
		this.existNested = false;
	}

	public String getOpenToken() {
		return openToken;
	}

	public String getCloseToken() {
		return closeToken;
	}

	public ParserWarper getWarper() {
		return warper;
	}

	public boolean isExistNested() {
		return existNested;
	}

	public boolean isAllowNested() {
		return allowNested;
	}

	public void setExistNested(boolean existNested) {
		this.existNested = existNested;
	}

}
