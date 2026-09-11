package org.example.librarymanagementsystem.exception;

public class AlreadyReturnedException extends RuntimeException {

    public AlreadyReturnedException(Integer borrowId) {
        super("Borrow record with id " + borrowId +
                " is already returned");
    }

    public AlreadyReturnedException(String message) {
        super(message);
    }
}
