package org.xson.tangyuan.rpc;

import org.xson.tangyuan.executor.ServiceException;

public class RpcException extends ServiceException {

	private static final long serialVersionUID = 1L;

	public RpcException(String message) {
		super(message);
	}
}
