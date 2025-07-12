package com.qlda.authservice.exception;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
public class AppException extends RuntimeException{

    private final ErrorCode errorCode;

    public AppException (ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
