package com.studentplanner.exception;

/**
 * Thrown when a requested resource (User, Subject, or Task) cannot be found in the database.
 * Results in an HTTP 404 Not Found response.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
