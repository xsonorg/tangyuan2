package org.xson.tangyuan.rpc;

import org.xson.tangyuan.executor.ServiceException;

public class RpcException extends ServiceException {

	private static final long serialVersionUID = 1L;

	public RpcException(String message) {
		super(message);
		this.errorCode = RpcComponent.getInstance().getErrorCode();
		this.errorMessage = RpcComponent.getInstance().getErrorMessage();
	}

	public RpcException(String message, Throwable cause) {
		super(message, cause);
		this.errorCode = RpcComponent.getInstance().getErrorCode();
		this.errorMessage = RpcComponent.getInstance().getErrorMessage();
	}

}
