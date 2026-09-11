package org.example.librarymanagementsystem.exception;

public class CategoryNotFoundException extends RuntimeException{
    public CategoryNotFoundException(Integer id) {
        super("Category with id " + id + " not found");
    }

    public CategoryNotFoundException(String message) {
        super(message);
    }
}
