package org.xson.tangyuan.hive.service.cmd;

import java.util.List;

import org.xson.common.object.ActuatorContextXCO;
import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.hive.transaction.XTransactionDefinition;
import org.xson.tangyuan.xml.node.AbstractServiceNode;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

/**
 * SQL命令执行器
 */
public class HIVE {

	private static AbstractServiceNode findService(String serviceURI) {
		AbstractServiceNode service = TangYuanContainer.getInstance().getService(serviceURI);
		// TODO 英文
		if (null == service) {
			throw new TangYuanException("不存在的SQL定义: " + serviceURI);
		}
		if (TangYuanServiceType.HIVE != service.getServiceType()) {
			throw new TangYuanException("当前服务不可当作SQL命令执行: " + serviceURI);
		}
		return service;
	}

	static class HIVECommandImpl implements HIVECommand {

		private HIVECommandContext sqlAc;

		protected HIVECommandImpl(HIVECommandContext sqlAc) {
			this.sqlAc = sqlAc;
		}

		@SuppressWarnings("unchecked")
		public <T> T execute(String sqlURI, Object arg) throws Throwable {
			// TODO 调用跟踪的问题
			try {
				AbstractServiceNode service = findService(sqlURI);
				service.execute(this.sqlAc, arg, new ActuatorContextXCO((XCO) arg));
				return (T) service.getResult(this.sqlAc);
			} catch (Throwable e) {
				// 异常上抛后，由于用户可能捕获，有可能会丢失异常，导致回滚出问题，因此在这里记录
				this.sqlAc.onException(e);
				// 异常全部上抛
				throw e;
			}
		}

		public List<XCO> selectSet(String sqlURI, XCO arg) throws Throwable {
			return execute(sqlURI, arg);
		}

		public XCO selectOne(String sqlURI, XCO arg) throws Throwable {
			return execute(sqlURI, arg);
		}

		public <T> T selectVar(String sqlURI, XCO arg) throws Throwable {
			return execute(sqlURI, arg);
		}

		public int update(String sqlURI, XCO arg) throws Throwable {
			return execute(sqlURI, arg);
		}

		public int delete(String sqlURI, XCO arg) throws Throwable {
			return execute(sqlURI, arg);
		}

		public <T> T insert(String sqlURI, XCO arg) throws Throwable {
			return execute(sqlURI, arg);
		}
	}

	/**
	 * 开始一个指定的SQL事物，并执行SQL上下文中的内容
	 * 
	 * @param <T>
	 * @param context
	 *            SQL上下文
	 * @param txRef
	 *            所使用SQL事物
	 * @return
	 * @throws Throwable
	 */
	@SuppressWarnings("unchecked")
	public static <T> T trans(HIVETransContext context, String txRef) throws Throwable {
		if (null == context) {
			throw new TangYuanException("'trans'方法中[context]参数为空");
		}

		XTransactionDefinition txDef  = null;

		HIVECommandContext     sqlAc  = new HIVECommandContext(txDef);
		Throwable              ex     = null;
		Object                 result = null;
		try {
			sqlAc.begin();
			result = context.execute(new HIVECommandImpl(sqlAc));
		} catch (Throwable e) {
			ex = e;
		}
		try {
			sqlAc.commitAndRollBack(ex);// can throw ex on exception
		} catch (Throwable e) {
			// 新的异常上抛
			throw e;
		}
		if (null != ex) {
			// 过程中的异常上抛
			throw ex;
		}
		return (T) result;
	}

}
