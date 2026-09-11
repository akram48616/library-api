package org.example.librarymanagementsystem.exception;

public class AuthorNotFoundException extends RuntimeException {
    public AuthorNotFoundException(Integer id) {
        super("Author with id " + id + " not found");
    }
    public AuthorNotFoundException(String message) {
        super(message);
    }
}
