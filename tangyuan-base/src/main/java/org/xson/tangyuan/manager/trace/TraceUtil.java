package org.xson.tangyuan.manager.trace;

import java.lang.management.ManagementFactory;
import java.util.UUID;

import org.xson.tangyuan.util.StringUtils;

/**
 * Trace工具类
 */
public class TraceUtil {

	private static String processNo;

	public static String getProcessNo() {
		if (StringUtils.isEmpty(processNo)) {
			String name = ManagementFactory.getRuntimeMXBean().getName();
			processNo = name.split("@")[0];
		}
		return processNo;
	}

	/**
	 * TraceId生成函数
	 * 
	 * A: IP地址							--8位16进制
	 * T: 时间戳（毫秒值）					--13位
	 * C: 4位（1000-9999）的整数递增量		--4位16进制
	 * P: 进程号/端口号						--4位16进制
	 * F: 扩展字符
	 */
	public static String createTraceId() {
		// aaaaaaaatttttttttttttccccfpppp
		long t = System.currentTimeMillis();
		String p = getProcessNo();
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	// trackingVo.setStringValue("host_ip", mlc.getNodeIp());
	// trackingVo.setStringValue("node_name", mlc.getNodeName());
	// trackingVo.setStringValue("node_port", mlc.getNodePort());
	// trackingVo.setStringValue("app_name", mlc.getAppName());

	public static String createSpanId() {
		// return null;
		// // TODO 考虑node and app
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	public static void main(String[] args) {
		long now = System.currentTimeMillis();
		System.out.println(now);
		String p = getProcessNo();
		System.out.println(p);

		System.out.println("abc".hashCode());
		System.out.println("1".hashCode());
		System.out.println("2".hashCode());
		System.out.println("a".hashCode());
		System.out.println("www.baidu.com".hashCode());
		System.out.println("a.xxzyzzz.www.baidu.com".hashCode());
		System.out.println("xcv.aixbx.com".hashCode());
	}
}
