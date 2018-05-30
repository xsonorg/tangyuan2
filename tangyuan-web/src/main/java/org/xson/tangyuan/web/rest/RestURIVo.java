package org.xson.tangyuan.web.rest;

import java.util.List;
import java.util.Map;

public class RestURIVo {

	public static final String		URI_SYMBOL_FOLDER_SEPARATOR	= "/";
	public static final String		URI_SYMBOL_ASTERISK			= "*";
	public static final String		URI_SYMBOL_HASHTAG			= "#";
	public static final String		URI_SYMBOL_QUESTION_MARK	= "?";
	public static final String		URI_SYMBOL_AND				= "&";
	public static final String		URI_SYMBOL_EQUAL			= "=";

	private String					path						= null;	// /a/{id}/b

	private List<String>			patternList					= null;	// [a,#,b]

	/** key: pos, value:varName */
	private Map<Integer, String>	pathVariables				= null;

	/** key: originalVarName, value:customVarName */
	private Map<String, String>		queryVariables				= null;

	private boolean					staticPath					= false;

	public RestURIVo(String path, List<String> patternList, Map<Integer, String> pathVariables, Map<String, String> queryVariables) {
		this.path = path;
		this.patternList = patternList;
		this.queryVariables = queryVariables;
		this.pathVariables = pathVariables;
		if (null == patternList) {
			this.staticPath = true;
		}
	}

	public RestURIVo(List<String> patternList) {
		this.patternList = patternList;
	}

	public List<String> getPatternList() {
		return patternList;
	}

	public String getPath() {
		return this.path;
	}

	public boolean isStaticURI() {
		return staticPath;
	}

	public Map<Integer, String> getPathVariables() {
		return pathVariables;
	}

	public Map<String, String> getQueryVariables() {
		return queryVariables;
	}

}
