package com.studentplanner.exception;

/**
 * Thrown when an incoming request violates business validation rules
 * (e.g., trying to register with an email that is already registered).
 * Results in an HTTP 400 Bad Request response.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
