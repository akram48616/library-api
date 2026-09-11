package org.example.librarymanagementsystem.exception;

public class BorrowRecordNotFoundException extends RuntimeException {
    public BorrowRecordNotFoundException(Integer id) {
        super("Borrow record with id " + id + " not found");
    }
    public BorrowRecordNotFoundException(String message) {
        super(message);
    }
}
