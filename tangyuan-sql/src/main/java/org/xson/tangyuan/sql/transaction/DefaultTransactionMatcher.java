package org.xson.tangyuan.sql.transaction;

import java.util.List;
import java.util.Map;

import org.xson.tangyuan.util.PatternMatchUtils;
import org.xson.tangyuan.xml.XmlParseException;

import com.alibaba.fastjson.JSON;

public class DefaultTransactionMatcher {

	// command|method
	private String								type;

	private List<String[]>						ruleList;

	private Map<String, XTransactionDefinition>	transactionMap;

	public void setTypeAndRule(String type, List<String[]> ruleList) {
		this.type = type;
		this.ruleList = ruleList;
	}

	public void setTransactionMap(Map<String, XTransactionDefinition> transactionMap) {
		this.transactionMap = transactionMap;
	}

	public XTransactionDefinition getTransactionDefinition(String txRef, String method, String command) {
		if (null != txRef && txRef.length() > 0 && null != transactionMap) {
			return transactionMap.get(txRef);
		} else if ("method".equalsIgnoreCase(type) && null != this.ruleList) {
			String transactionKey = null;
			int count = 0;
			for (String[] rule : ruleList) {
				if (PatternMatchUtils.simpleMatch(rule[0], method)) {
					transactionKey = rule[1];
					count++;
				}
			}
			if (count == 0) {
				return null;
			} else if (count > 1) {
				throw new XmlParseException("该方法多次匹配:" + method);
			} else {
				return transactionMap.get(transactionKey);
			}
		} else if ("command".equalsIgnoreCase(type) && null != this.ruleList) {
			String transactionKey = null;
			int count = 0;
			for (String[] rule : ruleList) {
				if (rule[0].equalsIgnoreCase(command)) {
					transactionKey = rule[1];
					count++;
				}
			}
			if (count == 0) {
				return null;
			} else if (count > 1) {
				throw new XmlParseException("该方法多次匹配:" + method);
			} else {
				return transactionMap.get(transactionKey);
			}
		}
		return null;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return type + "\n" + JSON.toJSONString(ruleList) + "\n" + JSON.toJSONString(transactionMap);
	}
}
