package org.example.librarymanagementsystem.entity;



import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "borrow_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDate borrowDate;
    private LocalDate returnDate;
    private LocalDate dueDate;
    private double fine;

    @Enumerated(EnumType.STRING)
    private BorrowStatus status;

    // ─── @ManyToOne → Book ─────────────────────────
    // many borrow records → one book
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Book book;

    // ─── @ManyToOne → Member ───────────────────────
    // many borrow records → one member
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Member member;

    // Enum inside entity
    public enum BorrowStatus {
        BORROWED,
        RETURNED,
        OVERDUE
    }
}