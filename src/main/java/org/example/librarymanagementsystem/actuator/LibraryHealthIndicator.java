package org.example.librarymanagementsystem.actuator;

import lombok.RequiredArgsConstructor;
import org.example.librarymanagementsystem.Repository.BookRepository;
import org.example.librarymanagementsystem.Repository.BorrowRecordRepository;
import org.example.librarymanagementsystem.Repository.MemberRepository;
import org.example.librarymanagementsystem.entity.BorrowRecord;
import org.example.librarymanagementsystem.entity.Member;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component("library")
@RequiredArgsConstructor
public class LibraryHealthIndicator implements HealthIndicator {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final BorrowRecordRepository borrowRepository;

    @Override
    public Health health() {
        try {
            long totalBooks = bookRepository.count();
            long availableBooks = bookRepository.countByStockGreaterThan(0);
            long totalMembers = memberRepository.count();
            long activeMembers = memberRepository.countByMembershipStatus(Member.MembershipStatus.ACTIVE);
            long borrowedBooks = borrowRepository.countByStatus(BorrowRecord.BorrowStatus.BORROWED);
            long overdueBooks = borrowRepository.countByDueDateBeforeAndStatusNot(
                    LocalDate.now(), BorrowRecord.BorrowStatus.RETURNED);

            return Health.up()
                    .withDetail("totalBooks", totalBooks)
                    .withDetail("availableBooks", availableBooks)
                    .withDetail("totalMembers", totalMembers)
                    .withDetail("activeMembers", activeMembers)
                    .withDetail("borrowedBooks", borrowedBooks)
                    .withDetail("overdueBooks", overdueBooks)
                    .withDetail("status", "Library is running!")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("status", "Library health check failed")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}