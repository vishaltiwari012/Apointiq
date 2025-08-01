package com.cw.scheduler.exception;

public class OutsideWorkingHoursException extends RuntimeException{
    public OutsideWorkingHoursException(String message) {
        super(message);
    }
}
