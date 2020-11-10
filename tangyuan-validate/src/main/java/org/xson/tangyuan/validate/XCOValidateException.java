package org.xson.tangyuan.validate;

import org.xson.tangyuan.executor.ServiceException;

public class XCOValidateException extends ServiceException {

	private static final long serialVersionUID = 4225315797257223806L;

	public XCOValidateException(String message) {
		super(message);
		this.errorCode = ValidateComponent.getInstance().getErrorCode();
		this.errorMessage = ValidateComponent.getInstance().getErrorMessage();
	}

	public XCOValidateException(int errorCode, String errorMessage) {
		super(errorMessage);
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public XCOValidateException(int errorCode, String errorMessage, Throwable cause) {
		super(errorMessage, cause);
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

}
