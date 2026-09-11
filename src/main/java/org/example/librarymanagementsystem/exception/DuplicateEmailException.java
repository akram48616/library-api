package org.example.librarymanagementsystem.exception;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String email) {
        super("Account with email " + email + " already exists");
    }
}
