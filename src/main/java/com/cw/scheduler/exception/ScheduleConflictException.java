package com.cw.scheduler.exception;

public class ScheduleConflictException extends RuntimeException{
    public ScheduleConflictException(String message) {
        super(message);
    }
}
