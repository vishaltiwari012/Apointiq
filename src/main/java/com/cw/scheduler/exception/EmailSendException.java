package com.cw.scheduler.exception;

public class EmailSendException extends RuntimeException{
    public EmailSendException(String message) {
        super(message);
    }
}
