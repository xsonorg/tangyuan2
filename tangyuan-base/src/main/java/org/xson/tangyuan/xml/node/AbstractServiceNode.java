package org.xson.tangyuan.xml.node;

import org.xson.tangyuan.aop.AspectVo.PointCut;
import org.xson.tangyuan.executor.ServiceContext;

public abstract class AbstractServiceNode implements TangYuanNode {

	/** 服务类型 */
	public enum TangYuanServiceType {
		SQL, MONGO, HIVE, HBASE, JAVA, MQ, PRCPROXY, ES
	}

	/** 服务ID */
	protected String				id;

	/** 命名空间 */
	protected String				ns;

	/** 命名空间+ID */
	protected String				serviceKey;

	protected Class<?>				resultType;

	protected TangYuanServiceType	serviceType;

	protected int					aspect	= 0;

	public String getId() {
		return id;
	}

	public String getNs() {
		return ns;
	}

	public String getServiceKey() {
		return serviceKey;
	}

	public Class<?> getResultType() {
		return resultType;
	}

	public TangYuanServiceType getServiceType() {
		return serviceType;
	}

	public void setAspect(PointCut pointCut) {
		aspect = aspect | pointCut.value();
	}

	public boolean checkAspect(PointCut pointCut) {
		return (aspect & pointCut.value()) > 0;
	}

	/** 获取返回对象 */
	public Object getResult(ServiceContext context) {
		Object result = context.getResult();
		context.setResult(null);// 清理
		return result;
	}

	protected String getSlowServiceLog(long startTime) {
		long intervals = System.currentTimeMillis() - startTime;
		String slowLogInfo = "(";
		if (intervals >= 1000L) {
			slowLogInfo = slowLogInfo + "5level slow sql service ";
		} else if (intervals >= 500L) {
			slowLogInfo = slowLogInfo + "4level slow sql service ";
		} else if (intervals >= 300L) {
			slowLogInfo = slowLogInfo + "3level slow sql service ";
		} else if (intervals >= 200L) {
			slowLogInfo = slowLogInfo + "2level slow sql service ";
		} else if (intervals >= 100L) {
			slowLogInfo = slowLogInfo + "1level slow sql service ";
		}
		slowLogInfo = slowLogInfo + intervals + "ms)";
		return slowLogInfo;
	}

}
