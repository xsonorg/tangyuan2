package org.xson.tangyuan.xml.node;

import org.xson.common.object.XCO;
import org.xson.tangyuan.aop.service.vo.AopVo.PointCut;
import org.xson.tangyuan.service.ActuatorContext;

public abstract class AbstractServiceNode implements TangYuanNode {

	/**
	 * 服务类型
	 */
	public enum TangYuanServiceType {
		// NO, SQL, MONGO, HIVE, HBASE, JAVA, MQ, PRCPROXY, ES, VALIDATE, CACHE, CONVERT, GENERAL
		// NO, SQL, MONGO, HIVE, HBASE, JAVA, MQ, PRCPROXY, ES, VALIDATE, CACHE

		NO(0), SQL(1), JAVA(2), MONGO(3), ES(4), MQ(5), HIVE(6), HBASE(7), PRCPROXY(8), VALIDATE(9), CACHE(10), WEB(99);

		private int val;

		TangYuanServiceType(int val) {
			this.val = val;
		}

		public int getVal() {
			return val;
		}
	}

	/** 命名空间 */
	protected String              ns          = "";
	/** 服务ID */
	protected String              id          = null;
	/** 全服务名:[命名空间/ID] */
	protected String              serviceKey  = null;
	/** 返回类型 */
	protected Class<?>            resultType  = null;
	/** 服务类型 */
	protected TangYuanServiceType serviceType = null;
	/** 后面考虑:引入相关服务和实现类 TODO */
	protected int                 aspect      = 0;
	/** 服务的描述 */
	protected String              desc        = null;
	/** 服务组名 */
	protected String[]            groups      = null;

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

	public String[] getGroups() {
		return groups;
	}

	public String getDesc() {
		return desc;
	}

	/**
	 * 获取返回对象
	 * @param context
	 * @return
	 */
	public Object getResult(ActuatorContext context) {
		Object result = context.getResult();
		context.setResult(null);// 清理
		return result;
	}

	// TODO 这里可以考虑进行monitor统计
	protected String getSlowServiceLog(long startTime) {
		long   intervals   = System.currentTimeMillis() - startTime;
		String slowLogInfo = "(";
		if (intervals >= 1000L) {
			slowLogInfo = slowLogInfo + "5level slow service ";
		} else if (intervals >= 500L) {
			slowLogInfo = slowLogInfo + "4level slow service ";
		} else if (intervals >= 300L) {
			slowLogInfo = slowLogInfo + "3level slow service ";
		} else if (intervals >= 200L) {
			slowLogInfo = slowLogInfo + "2level slow service ";
		} else if (intervals >= 100L) {
			slowLogInfo = slowLogInfo + "1level slow service ";
		}
		slowLogInfo = slowLogInfo + intervals + "ms)";
		return slowLogInfo;
	}

	@Override
	public boolean execute(ActuatorContext ac, Object arg, Object acArg) throws Throwable {
		return false;
	}

	protected Object cloneArg(Object arg) {
		if (arg instanceof XCO) {
			return ((XCO) arg).clone();
		}
		return arg;
	}

	/** 服务前缀: 不合适 */
	//  protected String basePrefix = "";
	//	@Override
	//	public boolean execute(ActuatorContext sc, Object arg, Object temp) throws Throwable {
	//		return false;
	//	}
}
