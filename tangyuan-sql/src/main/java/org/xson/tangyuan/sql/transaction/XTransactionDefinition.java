package org.xson.tangyuan.sql.transaction;

import java.sql.Connection;

public class XTransactionDefinition {

	// 表示当前方法必须在一个具有事务的上下文中运行，如有客户端有事务在进行，那么被调用端将在该事务中运行，否则的话重新开启一个事务。
	// （如果被调用端发生异常，那么调用端和被调用端事务都将回滚）
	public static final int	PROPAGATION_REQUIRED		= 0;

	// 表示当前方法不必需要具有一个事务上下文，但是如果有一个事务的话，它也可以在这个事务中运行
	public static final int	PROPAGATION_SUPPORTS		= 1;

	// 表示当前方法必须在一个事务中运行，如果没有事务，将抛出异常
	public static final int	PROPAGATION_MANDATORY		= 2;

	// 表示当前方法必须运行在它自己的事务中。一个新的事务将启动，而且如果有一个现有的事务在运行的话，则这个方法将在运行期被挂起，直到新的事务提交或者回滚才恢复执行。
	// 挂起之前的，先执行它
	public static final int	PROPAGATION_REQUIRES_NEW	= 3;

	// 表示该方法不应该在一个事务中运行。如果有一个事务正在运行，他将在运行期被挂起，直到这个事务提交或者回滚才恢复执行
	// 以非事务方式执行操作，如果当前存在事务，就把当前事务挂起。
	public static final int	PROPAGATION_NOT_SUPPORTED	= 4;

	// 表示当方法务不应该在一个事务中运行，如果存在一个事务，则抛出异常
	public static final int	PROPAGATION_NEVER			= 5;

	// 表示如果当前方法正有一个事务在运行中，则该方法应该运行在一个嵌套事务中，被嵌套的事务可以独立于被封装的事务中进行提交或者回滚。
	// 如果封装事务存在，并且外层事务抛出异常回滚，那么内层事务必须回滚，反之，内层事务并不影响外层事务。如果封装事务不存在，则同PROPAGATION_REQUIRED的一样
	public static final int	PROPAGATION_NESTED			= 6;

	/**
	 * 默认事务隔离级别，具体使用的数据库事务隔离级别由底层决定。
	 * 
	 * @see java.sql.Connection
	 */
	public static final int	ISOLATION_DEFAULT			= -1;

	/**
	 * 脏读
	 * <p>
	 * 脏读又称无效数据的读出，是指在数据库访问中，事务T1将某一值修改，然后事务T2读取该值，此后T1因为某种原因撤销对该值的修改，这就导致了T2所读取到的数据是无效的。
	 * <p>
	 * 脏读就是指当一个事务正在访问数据，并且对数据进行了修改，而这种修改还没有提交到数据库中，这时，另外一个事务也访问这个数据，然后使用了这个数据。 因为这个数据是还没有提交的数据，那么另外一个事务读到的这个数据是脏数据，依据脏数据所做的操作可能是不正确的。
	 * 
	 * @see java.sql.Connection#TRANSACTION_READ_UNCOMMITTED
	 */
	public static final int	ISOLATION_READ_UNCOMMITTED	= Connection.TRANSACTION_READ_UNCOMMITTED;

	/**
	 * 不可重复读
	 * <p>
	 * 不可重复读，是指在数据库访问中，一个事务范围内两个相同的查询却返回了不同数据。
	 * <p>
	 * 这是由于查询时系统中其他事务修改的提交而引起的。比如事务T1读取某一数据，事务T2读取并修改了该数据，T1为了对读取值进行检验而再次读取该数据，便得到了不同的结果。
	 * <p>
	 * 一种更易理解的说法是：在一个事务内，多次读同一个数据。在这个事务还没有结束时，另一个事务也访问该同一数据。 那么，在第一个事务的两次读数据之间。由于第二个事务的修改，那么第一个事务读到的数据可能不一样，这样就发生了在一个事务内两次读到的数据是不一样的，因此称为不可重复读，即原始读取不可重复。
	 * 
	 * @see java.sql.Connection#TRANSACTION_READ_COMMITTED
	 */
	public static final int	ISOLATION_READ_COMMITTED	= Connection.TRANSACTION_READ_COMMITTED;
	/**
	 * 可重复读取
	 * <p>
	 * 可重复读(Repeatable Read)，当使用可重复读隔离级别时，在事务执行期间会锁定该事务以任何方式引用的所有行。 因此，如果在同一个事务中发出同一个SELECT语句两次或更多次，那么产生的结果数据集总是相同的。 因此，使用可重复读隔离级别的事务可以多次检索同一行集，并对它们执行任意操作，直到提交或回滚操作终止该事务。
	 * 
	 * @see java.sql.Connection#TRANSACTION_REPEATABLE_READ
	 */
	public static final int	ISOLATION_REPEATABLE_READ	= Connection.TRANSACTION_REPEATABLE_READ;
	/**
	 * 同步事务
	 * <p>
	 * 提供严格的事务隔离。它要求事务序列化执行，事务只能一个接着一个地执行，但不能并发执行。
	 * <p>
	 * 如果仅仅通过“行级锁”是无法实现事务序列化的，必须通过其他机制保证新插入的数据不会被刚执行查询操作的事务访问到。
	 * 
	 * @see java.sql.Connection#TRANSACTION_SERIALIZABLE
	 */
	public static final int	ISOLATION_SERIALIZABLE		= Connection.TRANSACTION_SERIALIZABLE;

	public static final int	TIMEOUT_DEFAULT				= -1;

	/****************************************************************************************************/

	private String			id							= null;

	// 事务定义的名称, 默认为服务ID, 共后面运行时引用
	private String			name						= null;
	// 传播行为
	private int				behavior					= PROPAGATION_REQUIRED;
	// 隔离级别
	private int				isolation					= ISOLATION_DEFAULT;
	// 超时时间
	private int				timeout						= TIMEOUT_DEFAULT;
	// 是否只读
	private boolean			readOnly					= false;
	// 使用之前存在的事务(运行时概念)
	private String			txUse						= null;

	// 是否是xa事务
	// private boolean xa = false;

	// 分布式事务处理策略[0:不使用,1:Best Efforts 1PC, 2:两阶段提交] 默认0
	// private int xaStrategy = 0;

	public XTransactionDefinition(String txUse) {
		this.txUse = txUse;
	}

	public XTransactionDefinition(String id, String name, Integer behavior, Integer isolation, Integer timeout, Boolean readOnly) {
		if (null != id) {
			this.id = id;
		}
		if (null != name) {
			this.name = name;
		}
		if (null != behavior) {
			this.behavior = behavior;
		}
		if (null != isolation) {
			this.isolation = isolation;
		}
		if (null != timeout) {
			this.timeout = timeout;
		}
		if (null != readOnly) {
			this.readOnly = readOnly;
		}
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getBehavior() {
		return behavior;
	}

	public int getIsolation() {
		return isolation;
	}

	public int getTimeout() {
		return timeout;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public String getTxUse() {
		return txUse;
	}

	public boolean isNewTranscation() {
		if (PROPAGATION_REQUIRES_NEW == behavior || PROPAGATION_NOT_SUPPORTED == behavior) {
			return true;
		}
		return false;
	}

	protected boolean isAutoCommit() {
		if (PROPAGATION_SUPPORTS == behavior || PROPAGATION_NOT_SUPPORTED == behavior || PROPAGATION_NEVER == behavior) {
			return true;
		}
		return false;
	}
}
