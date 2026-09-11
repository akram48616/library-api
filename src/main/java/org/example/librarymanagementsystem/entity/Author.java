package org.example.librarymanagementsystem.entity;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "author")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String firstname;
    private String lastname;
    private String email;
    private String nationality;

    // inverse side of ManyToMany
    @ManyToMany(mappedBy = "authors")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Book> books = new ArrayList<>();
}