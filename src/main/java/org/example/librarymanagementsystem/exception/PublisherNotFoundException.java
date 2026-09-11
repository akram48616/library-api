package org.example.librarymanagementsystem.exception;

public class PublisherNotFoundException extends RuntimeException {
    public PublisherNotFoundException(Integer id) {
        super("Publisher with id " + id + " not found");
    }
    public PublisherNotFoundException(String message) {
        super(message);
    }
}
