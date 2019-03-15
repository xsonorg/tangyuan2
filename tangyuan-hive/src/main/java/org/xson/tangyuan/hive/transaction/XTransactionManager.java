package org.xson.tangyuan.hive.transaction;

import java.sql.SQLException;

public interface XTransactionManager {

	XTransactionStatus getTransaction(String dsKey, XTransactionDefinition definition, XTransactionStatus status);

	XTransactionStatus commit(XTransactionStatus status, boolean confirm) throws SQLException;

	XTransactionStatus rollback(XTransactionStatus status) throws SQLException;

}
