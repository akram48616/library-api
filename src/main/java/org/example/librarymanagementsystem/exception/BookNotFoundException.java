package org.example.librarymanagementsystem.exception;

public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(Integer id){
        super("Book with id "+id+" not found");
    }
    public BookNotFoundException(String message) {
        super(message);
    }
}
