package org.example.librarymanagementsystem.exception;

public class BookNotAvailableException extends RuntimeException {
    public BookNotAvailableException(Integer bookId) {
        super("Book with id " + bookId +
                " is not available. Out of stock!");
    }
    public BookNotAvailableException(String message) {
        super(message);
    }
}
