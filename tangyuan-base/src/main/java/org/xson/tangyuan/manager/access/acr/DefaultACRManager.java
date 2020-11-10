package org.xson.tangyuan.manager.access.acr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.xson.tangyuan.manager.access.acr.ServiceRuleItemVo.ServiceMatchType;
import org.xson.tangyuan.util.PatternMatchUtils;

/**
 * ACR(access control rule)管理器
 */
public class DefaultACRManager {

	/**应用级别的规则*/
	private AppRuleSet                  appRules;

	/**服务级的精确白名单*/
	private Map<String, ServiceRuleSet> preciseServiceRules;

	/**服务级的模糊白名单*/
	private List<ServiceRuleSet>        fuzzyServiceRules;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void init(List<AppRuleItemVo> appItems, List<ServiceRuleItemVo> serviceItems) {
		if (null != appItems) {
			this.appRules = new AppRuleSet();
			this.appRules.init(appItems);
		}

		if (null != serviceItems) {
			Map<String, List<ServiceRuleItemVo>> tempPreciseMap = new HashMap<String, List<ServiceRuleItemVo>>();
			Map<String, List<ServiceRuleItemVo>> tempFuzzyMap   = new HashMap<String, List<ServiceRuleItemVo>>();

			// pre
			for (ServiceRuleItemVo sriVo : serviceItems) {
				ServiceMatchType serviceMatchType = sriVo.getServiceMatchType();
				if (ServiceMatchType.FUZZY == serviceMatchType) {
					List<ServiceRuleItemVo> list = tempFuzzyMap.get(sriVo.getService());
					if (null == list) {
						list = new ArrayList<ServiceRuleItemVo>();
						tempFuzzyMap.put(sriVo.getService(), list);
					}
					list.add(sriVo);
				} else if (ServiceMatchType.PRECISE == serviceMatchType) {
					List<ServiceRuleItemVo> list = tempPreciseMap.get(sriVo.getService());
					if (null == list) {
						list = new ArrayList<ServiceRuleItemVo>();
						tempPreciseMap.put(sriVo.getService(), list);
					}
					list.add(sriVo);
				}
			}

			// init
			if (!tempPreciseMap.isEmpty()) {
				preciseServiceRules = new HashMap<String, ServiceRuleSet>();
				for (Entry<String, List<ServiceRuleItemVo>> entry : tempPreciseMap.entrySet()) {
					ServiceRuleSet srs = new ServiceRuleSet();
					srs.init((List) entry.getValue());
					preciseServiceRules.put(entry.getKey(), srs);
				}
			}
			if (!tempFuzzyMap.isEmpty()) {
				fuzzyServiceRules = new ArrayList<ServiceRuleSet>();
				for (Entry<String, List<ServiceRuleItemVo>> entry : tempFuzzyMap.entrySet()) {
					ServiceRuleSet srs = new ServiceRuleSet();
					srs.init((List) entry.getValue());
					fuzzyServiceRules.add(srs);
				}
			}
		}
	}

	public boolean check(String service, String remoteIp, String remoteDomain) {

		boolean result = true;

		if (null != appRules) {
			result = appRules.check(service, remoteIp, remoteDomain);
			if (!result) {
				return result;
			}
		}

		if (null != preciseServiceRules) {
			ServiceRuleSet srs = preciseServiceRules.get(service);
			if (null != srs) {
				result = srs.check(service, remoteIp, remoteDomain);
				if (!result) {
					return result;
				}
			}
		}

		if (null != fuzzyServiceRules) {
			for (ServiceRuleSet rs : fuzzyServiceRules) {
				if (PatternMatchUtils.simpleMatch(rs.getService(), service)) {
					result = rs.check(service, remoteIp, remoteDomain);
					if (!result) {
						return result;
					}
				}
			}
		}

		return true;
	}
}
