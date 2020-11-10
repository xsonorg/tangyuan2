package org.xson.tangyuan.manager.access.acr;

import java.util.ArrayList;
import java.util.List;

import org.xson.tangyuan.manager.access.acr.AppRuleItemVo.MatchType;
import org.xson.tangyuan.util.PatternMatchUtils;

public class RuleSet {

	// 黑名单相关

	/* <rule type="黑名单:不允许的域名" value="*" /> */
	protected boolean      blackListDomainAll     = false;
	/* <rule type="黑名单:不允许的域名" value="api.aixbx.com" /> */
	protected List<String> blackListDomainPrecise = null;
	/* <rule type="黑名单:不允许的域名" value="*.aixbx.com" /> */
	protected List<String> blackListDomainFuzzy   = null;

	/* <rule type="黑名单:不允许的IP" value="*" /> */
	protected boolean      blackListIPAll         = false;
	/* <rule type="黑名单:不允许的IP" value="192.168.50.222" /> */
	protected List<String> blackListIPPrecise     = null;
	/* <rule type="黑名单:不允许的IP" value="192.168.50.*" /> */
	protected List<String> blackListIPFuzzy       = null;
	/* <rule type="黑名单:不允许的IP" value="192.168.50.248/8" /> */
	protected List<String> blackListIPFuzzy1      = null;

	// 白名单相关

	protected boolean      hasWhiteList           = false;

	/* <rule type="白名单:允许的域名" value="*" /> */
	protected boolean      whiteListDomainAll     = false;
	/* <rule type="白名单:允许的域名" value="api.aixbx.com" /> */
	protected List<String> whiteListDomainPrecise = null;
	/* <rule type="白名单:允许的域名" value="*.aixbx.com" /> */
	protected List<String> whiteListDomainFuzzy   = null;

	/* <rule type="白名单:允许的IP" value="*" /> */
	protected boolean      whiteListIPAll         = false;
	/* <rule type="白名单:允许的IP" value="192.168.50.222" /> */
	protected List<String> whiteListIPPrecise     = null;
	/* <rule type="白名单:允许的IP" value="192.168.50.*" /> */
	protected List<String> whiteListIPFuzzy       = null;
	/* <rule type="白名单:允许的IP" value="192.168.50.248/8" /> */
	protected List<String> whiteListIPFuzzy1      = null;

	protected void initBlackDomain(AppRuleItemVo vo) {
		MatchType matchType = vo.getMatchType();
		if (MatchType.PRECISE == matchType) {
			if (null == blackListDomainPrecise) {
				blackListDomainPrecise = new ArrayList<String>();
			}
			blackListDomainPrecise.add(vo.getValue());
		} else if (MatchType.FUZZY == matchType) {
			if (null == blackListDomainFuzzy) {
				blackListDomainFuzzy = new ArrayList<String>();
			}
			blackListDomainFuzzy.add(vo.getValue());
		} else if (MatchType.ALL == matchType) {
			blackListDomainAll = true;
		}
	}

	protected void initBlackIp(AppRuleItemVo vo) {
		MatchType matchType = vo.getMatchType();
		if (MatchType.PRECISE == matchType) {
			if (null == blackListIPPrecise) {
				blackListIPPrecise = new ArrayList<String>();
			}
			blackListIPPrecise.add(vo.getValue());
		} else if (MatchType.FUZZY == matchType) {
			if (null == blackListIPFuzzy) {
				blackListIPFuzzy = new ArrayList<String>();
			}
			blackListIPFuzzy.add(vo.getValue());
		} else if (MatchType.FUZZY1 == matchType) {
			if (null == blackListIPFuzzy1) {
				blackListIPFuzzy1 = new ArrayList<String>();
			}
			blackListIPFuzzy1.add(vo.getValue());
		} else if (MatchType.ALL == matchType) {
			blackListIPAll = true;
		}
	}

	protected void initWhiteDomain(AppRuleItemVo vo) {
		MatchType matchType = vo.getMatchType();
		if (MatchType.PRECISE == matchType) {
			if (null == whiteListDomainPrecise) {
				whiteListDomainPrecise = new ArrayList<String>();
			}
			whiteListDomainPrecise.add(vo.getValue());
		} else if (MatchType.FUZZY == matchType) {
			if (null == whiteListDomainFuzzy) {
				whiteListDomainFuzzy = new ArrayList<String>();
			}
			whiteListDomainFuzzy.add(vo.getValue());
		} else if (MatchType.ALL == matchType) {
			whiteListDomainAll = true;
		}
		this.hasWhiteList = true;
	}

	protected void initWhiteIp(AppRuleItemVo vo) {
		MatchType matchType = vo.getMatchType();
		if (MatchType.PRECISE == matchType) {
			if (null == whiteListIPPrecise) {
				whiteListIPPrecise = new ArrayList<String>();
			}
			whiteListIPPrecise.add(vo.getValue());
		} else if (MatchType.FUZZY == matchType) {
			if (null == whiteListIPFuzzy) {
				whiteListIPFuzzy = new ArrayList<String>();
			}
			whiteListIPFuzzy.add(vo.getValue());
		} else if (MatchType.FUZZY1 == matchType) {
			if (null == whiteListIPFuzzy1) {
				whiteListIPFuzzy1 = new ArrayList<String>();
			}
			whiteListIPFuzzy1.add(vo.getValue());
		} else if (MatchType.ALL == matchType) {
			whiteListIPAll = true;
		}
		this.hasWhiteList = true;
	}

	/**
	 * isInRange("192.168.1.1", "192.168.0.0/22")
	 */
	protected boolean isInRange(String ip, String cidr) {
		String[] ips        = ip.split("\\.");
		int      ipAddr     = (Integer.parseInt(ips[0]) << 24) | (Integer.parseInt(ips[1]) << 16) | (Integer.parseInt(ips[2]) << 8)
				| Integer.parseInt(ips[3]);
		int      type       = Integer.parseInt(cidr.replaceAll(".*/", ""));
		int      mask       = 0xFFFFFFFF << (32 - type);
		String   cidrIp     = cidr.replaceAll("/.*", "");
		String[] cidrIps    = cidrIp.split("\\.");
		int      cidrIpAddr = (Integer.parseInt(cidrIps[0]) << 24) | (Integer.parseInt(cidrIps[1]) << 16) | (Integer.parseInt(cidrIps[2]) << 8)
				| Integer.parseInt(cidrIps[3]);

		return (ipAddr & mask) == (cidrIpAddr & mask);
	}
	
	public boolean check(String service, String remoteIp, String remoteDomain) {

		if (blackListDomainAll || blackListIPAll) {
			return false;
		}

		if (whiteListDomainAll || whiteListIPAll) {
			return true;
		}

		if (null == remoteIp || null == remoteDomain) {
			return false;
		}

		/* 1. 先判断黑名单 */

		// 1.1 精确的
		if (null != this.blackListIPPrecise) {
			for (String target : this.blackListIPPrecise) {
				if (target.equalsIgnoreCase(remoteIp)) {
					return false;
				}
			}
		}
		if (null != this.blackListDomainPrecise) {
			for (String target : this.blackListDomainPrecise) {
				if (target.equalsIgnoreCase(remoteDomain)) {
					return false;
				}
			}
		}

		// 1.2 模糊的
		if (null != this.blackListIPFuzzy) {
			for (String pattern : this.blackListIPFuzzy) {
				if (PatternMatchUtils.simpleMatch(pattern, remoteIp)) {
					return false;
				}
			}
		}
		if (null != this.blackListIPFuzzy1) {
			for (String target : this.blackListIPFuzzy1) {
				if (isInRange(remoteIp, target)) {
					return false;
				}
			}
		}
		if (null != this.blackListDomainFuzzy) {
			for (String pattern : this.blackListDomainFuzzy) {
				if (PatternMatchUtils.simpleMatch(pattern, remoteDomain)) {
					return false;
				}
			}
		}

		/* 2. 判断白名单 */

		// 2.1 精确的
		if (null != this.whiteListIPPrecise) {
			for (String target : this.whiteListIPPrecise) {
				if (target.equalsIgnoreCase(remoteIp)) {
					return true;
				}
			}
		}
		if (null != this.whiteListDomainPrecise) {
			for (String target : this.whiteListDomainPrecise) {
				if (target.equalsIgnoreCase(remoteDomain)) {
					return true;
				}
			}
		}

		// 1.2 模糊的
		if (null != this.whiteListIPFuzzy) {
			for (String pattern : this.whiteListIPFuzzy) {
				if (PatternMatchUtils.simpleMatch(pattern, remoteIp)) {
					return true;
				}
			}
		}
		if (null != this.whiteListIPFuzzy1) {
			for (String target : this.whiteListIPFuzzy1) {
				if (isInRange(remoteIp, target)) {
					return true;
				}
			}
		}
		if (null != this.whiteListDomainFuzzy) {
			for (String pattern : this.whiteListDomainFuzzy) {
				if (PatternMatchUtils.simpleMatch(pattern, remoteDomain)) {
					return true;
				}
			}
		}

		if (this.hasWhiteList) {
			// 如果存在白名单规则，说明之前未匹配
			return false;
		}
		return true;
	}
}
