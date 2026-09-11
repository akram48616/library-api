package org.example.librarymanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name="book")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String title;
    @Column(unique = true,nullable = false)
    private String isbn;
    private double price;
    private int stock;
    private LocalDate publishedDate;
    // ─── @ManyToOne → Publisher ────────────────────
    // many books → one publisher
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="publisher_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Publisher publisher;
    // ─── @ManyToMany → Author ──────────────────────
    // one book → many authors
    // one author → many books
    @ManyToMany(cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    @JoinTable(name = "book_author",
            joinColumns = @JoinColumn(name = "book_id"),
    inverseJoinColumns = @JoinColumn(name="author_id"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Author> authors=new ArrayList<>();

    // ─── @ManyToMany → Category ────────────────────
    // one book → many categories
    // one category → many books
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "book_category",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Category> categories = new HashSet<>();
    // ─── @OneToMany → BorrowRecord ─────────────────
    // one book → many borrow records
    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<BorrowRecord> borrowRecords = new ArrayList<>();
    @Column(name = "cover_image_url")
    private String coverImageUrl;
    @Column(name="cover-image_name")
    private String coverImageName;
    @Column(name="cover_image_type")
    private String coverImageType;
    @Column(name="cover_image_size")
    private Long coverImageSize;
}
