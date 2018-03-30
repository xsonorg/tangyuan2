package org.xson.tangyuan.mongo.executor.cmd;

import java.util.List;

public class CommandVo {

	private String			action;

	private List<String>	parameters;

	public String getAction() {
		return action;
	}

	public List<String> getParameters() {
		return parameters;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(action);
		if (null != parameters && parameters.size() > 0) {
			sb.append("(");
			for (String p : parameters) {
				sb.append(p).append(",");
			}
			sb.append(")");
		}else{
			sb.append("#");
		}
		return sb.toString();
	}
}
