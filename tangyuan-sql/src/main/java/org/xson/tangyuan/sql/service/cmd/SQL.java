package org.xson.tangyuan.sql.service.cmd;

import java.util.List;

import org.xson.common.object.ActuatorContextXCO;
import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.sql.SqlComponent;
import org.xson.tangyuan.sql.transaction.XTransactionDefinition;
import org.xson.tangyuan.xml.node.AbstractServiceNode;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

/**
 * SQL命令执行器
 */
public class SQL {

	private static AbstractServiceNode findService(String serviceURI) {
		AbstractServiceNode service = TangYuanContainer.getInstance().getService(serviceURI);
		// TODO 英文
		if (null == service) {
			throw new TangYuanException("不存在的SQL定义: " + serviceURI);
		}
		if (TangYuanServiceType.SQL != service.getServiceType()) {
			throw new TangYuanException("当前服务不可当作SQL命令执行: " + serviceURI);
		}
		return service;
	}

	static class SQLCommandImpl implements SQLCommand {

		private SQLCommandContext sqlAc;

		protected SQLCommandImpl(SQLCommandContext sqlAc) {
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
	 * @param <T>
	 * @param context	SQL上下文
	 * @param txRef		所使用SQL事物
	 * @return
	 * @throws Throwable
	 */
	@SuppressWarnings("unchecked")
	public static <T> T trans(SqlTransContext context, String txRef) throws Throwable {
		if (null == context) {
			throw new TangYuanException("'trans'方法中[context]参数为空");
		}

		XTransactionDefinition txDef = SqlComponent.getInstance().getTransactionMatcher().getTransactionDefinition(txRef);
		if (null == txDef) {
			throw new TangYuanException("不存在的事物定义: " + txRef);
		}
		//SQLCommandContext sqlAc  = new SQLCommandContext(null, txDef);
		//		SQLCommand        cmd    = new SQLCommandImpl(sqlAc);

		SQLCommandContext sqlAc  = new SQLCommandContext(txDef);
		Throwable         ex     = null;
		Object            result = null;
		try {
			sqlAc.begin();
			result = context.execute(new SQLCommandImpl(sqlAc));
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

	public static void main(String[] args) throws Throwable {
		//		SQL    sql    = new SQL();
		//
		//		String sqlURI = null;
		//		XCO    arg    = null;
		//		XCO    res    = sql.trans(new SqlTransContext() {
		//							@Override
		//							public Object execute() {
		//								return sql.selectOne(sqlURI, arg);
		//							}
		//						}, "tx01");
		//		XCO    res1   = sql.trans(() -> {
		//							return sql.selectSet(sqlURI, arg);
		//						}, "tx01");
		//		XCO    res2   = sql.trans(() -> sql.selectSet(sqlURI, arg), "tx01");
		//		XCO    res3   = sql.trans(() -> {
		//							XCO a    = sql.selectOne(sqlURI, arg);
		//							XCO b    = sql.selectOne(sqlURI, arg);
		//							XCO c    = sql.selectOne(sqlURI, arg);
		//							XCO resx = new XCO();
		//							resx.setXCOValue("a", a);
		//							resx.setXCOValue("b", b);
		//							resx.setXCOValue("c", c);
		//							return resx;
		//						}, "tx01");
		//		int    x      = sql.trans(() -> sql.selectSet(sqlURI, arg), "tx01");

		String sqlURI = null;
		XCO    arg    = null;
		XCO    res    = SQL.trans((x) -> x.selectSet(sqlURI, arg), "tx01");
		XCO    res3   = SQL.trans((x) -> {
							XCO a    = x.selectOne(sqlURI, arg);
							XCO b    = x.selectOne(sqlURI, arg);
							XCO c    = x.selectOne(sqlURI, arg);
							XCO resx = new XCO();
							resx.setXCOValue("a", a);
							resx.setXCOValue("b", b);
							resx.setXCOValue("c", c);
							return resx;
						}, "tx01");
		System.out.println(res);
		System.out.println(res3);
	}

	//	public long beginTransaction(String txRef) throws ServiceException {
	//		return -1L;
	//	}
	//	public long begin(String txRef) throws ServiceException {
	//		return -1L;
	//	}
	//	public void commit() throws ServiceException {
	//	}
	//	public void commit(long txID) throws ServiceException {
	//	}
	//	public void rollback() throws ServiceException {
	//	}
	//	public void rollback(long txID) throws ServiceException {
	//	}
	//	public void commitAndRollback() {
	//	}
	//	public <T> T trans(Runnable r) throws ServiceException {
	//	}
	//	public <T> T execute(String sqlURI, Object arg, String txRef) throws ServiceException {
	//		return null;
	//	}
	//	public List<XCO> selectSet(String sqlURI, XCO arg, String txRef) throws ServiceException {
	//		return null;//
	//	}
	//	public XCO selectOne(String sqlURI, XCO arg, String txRef) throws ServiceException {
	//		return null;//
	//	}
	//	public int delete(String sqlURI, XCO arg, String txRef) throws ServiceException {
	//		return 0;//
	//	}
	//	public <T> T selectVar(String sqlURI, XCO arg, String txRef) throws ServiceException {
	//		return null;/
	//	}
	//	public int update(String sqlURI, XCO arg, String txRef) throws ServiceException {
	//		return 0;//
	//	}
	//	public <T> T insert(String sqlURI, XCO arg, String txRef) throws ServiceException {
	//		return null;//
	//	}
	//		public <T> T execute(String sqlURI, Object arg) throws ServiceException {
	//			return null;
	//		}
	//
	//		public List<XCO> selectSet(String sqlURI, XCO arg) throws ServiceException {
	//			return null;
	//		}
}
