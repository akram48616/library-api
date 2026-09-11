package org.example.librarymanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="member")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private LocalDate membershipDate;

    @Enumerated(EnumType.STRING)
    private MembershipStatus membershipStatus;
    // one member has many borrow records
    @OneToMany(mappedBy = "member" ,fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<BorrowRecord> borrowRecords=new ArrayList<>();

    // Enum inside entity
    public enum MembershipStatus{
        ACTIVE,
        INACTIVE,
        SUSPENDED;
    }


}
