package org.example.librarymanagementsystem.exception;

public class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException(Integer id) {
        super("Member with id " + id + " not found");
    }
    public MemberNotFoundException(String message) {
        super(message);
    }
}
