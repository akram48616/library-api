package org.example.librarymanagementsystem.actuator;

import org.springframework.boot.actuate.info.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class  LibraryInfoContributor
        implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("library", Map.of(
                "name",        "Library Management System",
                "version",     "1.0.0",
                "author",      "Akram",
                "description", "Complete Library REST API",
                "startTime",   LocalDateTime.now().toString(),
                "features", Map.of(
                        "books",      "CRUD + Search + Pagination",
                        "members",    "CRUD + Status Management",
                        "borrowing",  "Borrow + Return + Fine Calc",
                        "authors",    "CRUD + Book Association",
                        "categories", "CRUD + Book Association"
                )
        ));
    }
}