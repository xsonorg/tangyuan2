package org.xson.tangyuan.executor;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.TangYuanException;

public class ServiceException extends TangYuanException {

	private static final long serialVersionUID = 1L;

	protected int             errorCode        = TangYuanContainer.getInstance().getErrorCode();
	protected String          errorMessage     = TangYuanContainer.getInstance().getErrorMessage();

	public ServiceException() {
		super();
	}

	public ServiceException(String message) {
		super(message);
	}

	public ServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServiceException(Throwable cause) {
		super(cause);
	}

	public ServiceException(int errorCode, String errorMessage) {
		super(errorMessage);
		this.errorCode = errorCode;
		if (null != errorMessage) {
			this.errorMessage = errorMessage;
		}
	}

	public ServiceException(int errorCode, String errorMessage, Throwable cause) {
		super(errorMessage, cause);
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}