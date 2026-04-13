package com.countrydelight.mms.exception;

import lombok.Getter;

@Getter
public class MmsException extends RuntimeException {
    private final String errorCode;

    public MmsException(String message) {
        super(message);
        this.errorCode = "MMS_ERROR";
    }

    public MmsException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public MmsException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "MMS_ERROR";
    }
}
