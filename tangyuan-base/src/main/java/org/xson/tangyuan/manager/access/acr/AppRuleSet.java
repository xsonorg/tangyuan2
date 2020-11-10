package org.xson.tangyuan.manager.access.acr;

import java.util.List;

import org.xson.tangyuan.manager.access.acr.AppRuleItemVo.RuleType;

public class AppRuleSet extends RuleSet {

	public void init(List<AppRuleItemVo> list) {
		for (AppRuleItemVo vo : list) {
			RuleType ruleType = vo.getRuleType();
			if (RuleType.BLACK_DOMAIN == ruleType) {
				initBlackDomain(vo);
			} else if (RuleType.BLACK_IP == ruleType) {
				initBlackIp(vo);
			} else if (RuleType.WHITE_DOMAIN == ruleType) {
				initWhiteDomain(vo);
			} else if (RuleType.WHITE_IP == ruleType) {
				initWhiteIp(vo);
			}
		}
		// 检测all
		if (null != blackListDomainPrecise || null != blackListDomainFuzzy) {
			blackListDomainAll = false;
		}
		if (null != blackListIPPrecise || null != blackListIPFuzzy || null != blackListIPFuzzy1) {
			blackListIPAll = false;
		}
		if (null != whiteListDomainPrecise || null != whiteListDomainFuzzy) {
			whiteListDomainAll = false;
		}
		if (null != whiteListIPPrecise || null != whiteListIPFuzzy || null != whiteListIPFuzzy1) {
			whiteListIPAll = false;
		}
	}



}
