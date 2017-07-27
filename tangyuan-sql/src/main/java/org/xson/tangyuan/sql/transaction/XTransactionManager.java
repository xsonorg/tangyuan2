package org.xson.tangyuan.sql.transaction;

import java.sql.SQLException;

public interface XTransactionManager {

	XTransactionStatus getTransaction(String dsKey, XTransactionDefinition definition, XTransactionStatus status);

	XTransactionStatus commit(XTransactionStatus status, boolean confirm) throws SQLException;

	XTransactionStatus rollback(XTransactionStatus status) throws SQLException;

	// XTransactionStatus getTransaction(String dsKey, XTransactionDefinition definition, XTransactionStatus status) throws SQLException;
	// XTransactionStatus commit(XTransactionStatus status, boolean confirm, SqlServiceContext context) throws SQLException;
	// void commit(XTransactionStatus status, boolean confirm) throws SQLException;
	// XTransactionStatus rollback(XTransactionStatus status, SqlServiceContext context) throws SQLException;
}
