package org.example.librarymanagementsystem.Repository;
import org.example.librarymanagementsystem.entity.BorrowRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowRecordRepository
        extends JpaRepository<BorrowRecord, Integer> {

    // ─── Derived Queries ───────────────────────────

    // find by member
    List<BorrowRecord> findByMemberId(Integer memberId);

    // find by book
    List<BorrowRecord> findByBookId(Integer bookId);

    // find by status
    List<BorrowRecord> findByStatus(BorrowRecord.BorrowStatus status);

    // find active borrow for book and member
    Optional<BorrowRecord> findByBookIdAndMemberIdAndStatus(
            Integer bookId,
            Integer memberId,
            BorrowRecord.BorrowStatus status);

    // ─── Pagination ────────────────────────────────

    // get all paged
    Page<BorrowRecord> findAll(Pageable pageable);

    // get by member paged
    Page<BorrowRecord> findByMemberId(
            Integer memberId, Pageable pageable);

    // get by status paged
    Page<BorrowRecord> findByStatus(
            BorrowRecord.BorrowStatus status, Pageable pageable);

    // ─── Custom @Query ─────────────────────────────

    // get borrow with book and member details
    @Query("SELECT DISTINCT b FROM BorrowRecord b " +
            "LEFT JOIN FETCH b.book " +
            "LEFT JOIN FETCH b.member " +
            "WHERE b.id = :id")
    Optional<BorrowRecord> findByIdWithDetails(
            @Param("id") Integer id);

    // get all borrows with details paged
    @Query(value = "SELECT DISTINCT b FROM BorrowRecord b " +
            "LEFT JOIN FETCH b.book " +
            "LEFT JOIN FETCH b.member",
            countQuery = "SELECT COUNT(b) FROM BorrowRecord b")
    Page<BorrowRecord> findAllWithDetails(Pageable pageable);

    // get overdue records
    @Query("SELECT b FROM BorrowRecord b " +
            "WHERE b.dueDate < :today " +
            "AND b.status = 'BORROWED'")
    List<BorrowRecord> findOverdueRecords(
            @Param("today") LocalDate today);

    // get member borrow history with details
    @Query("SELECT DISTINCT b FROM BorrowRecord b " +
            "LEFT JOIN FETCH b.book " +
            "WHERE b.member.id = :memberId")
    List<BorrowRecord> findByMemberIdWithDetails(
            @Param("memberId") Integer memberId);

    // get book borrow history
    @Query("SELECT DISTINCT b FROM BorrowRecord b " +
            "LEFT JOIN FETCH b.member " +
            "WHERE b.book.id = :bookId")
    List<BorrowRecord> findByBookIdWithDetails(
            @Param("bookId") Integer bookId);

    // update overdue status
    @Modifying
    @Transactional
    @Query("UPDATE BorrowRecord b SET b.status = 'OVERDUE' " +
            "WHERE b.dueDate < :today " +
            "AND b.status = 'BORROWED'")
    int markOverdueRecords(@Param("today") LocalDate today);

    // count borrows by status
    @Query("SELECT COUNT(b) FROM BorrowRecord b " +
            "WHERE b.status = :status")
    Long countByStatus(
            @Param("status") BorrowRecord.BorrowStatus status);

    // check if member has active borrow for this book
    boolean existsByBookIdAndMemberIdAndStatus(
            Integer bookId,
            Integer memberId,
            BorrowRecord.BorrowStatus status);
    long countByDueDateBeforeAndStatusNot(LocalDate date, BorrowRecord.BorrowStatus status);
}
