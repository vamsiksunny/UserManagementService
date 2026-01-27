package com.example.usermanagementservice.exceptions;

public class UserAlreadyExistingException extends RuntimeException {

    public UserAlreadyExistingException(String msg) {
        super(msg);
    }

}
