package org.example.librarymanagementsystem.Repository;

import org.example.librarymanagementsystem.Repository.*;
import org.example.librarymanagementsystem.entity.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("datajpa")
class BorrowRecordRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BorrowRecordRepository borrowRepository;

    private Book book;
    private Member member1;
    private Member member2;
    private Publisher publisher;
    private BorrowRecord activeRecord;
    private BorrowRecord returnedRecord;
    private BorrowRecord overdueRecord;

    @BeforeEach
    void setUp() {

        // publisher
        publisher = new Publisher();
        publisher.setName("Oxford Press");
        publisher.setAddress("Oxford");
        publisher.setPhone("9876543210");
        publisher.setBooks(new ArrayList<>());
        entityManager.persist(publisher);

        // book
        book = new Book();
        book.setTitle("Effective Java");
        book.setIsbn("9780134685991");
        book.setPrice(599.0);
        book.setStock(10);
        book.setPublisher(publisher);
        book.setAuthors(new ArrayList<>());
        book.setCategories(new HashSet<>());
        book.setBorrowRecords(new ArrayList<>());
        entityManager.persist(book);

        // member1
        member1 = new Member();
        member1.setFirstname("Akram");
        member1.setLastname("Shaik");
        member1.setEmail("akram@gmail.com");
        member1.setPhone("9502840779");
        member1.setMembershipDate(LocalDate.now());
        member1.setMembershipStatus(
                Member.MembershipStatus.ACTIVE);
        member1.setBorrowRecords(new ArrayList<>());
        entityManager.persist(member1);

        // member2
        member2 = new Member();
        member2.setFirstname("Sumiya");
        member2.setLastname("Shaik");
        member2.setEmail("sumiya@gmail.com");
        member2.setPhone("9985510280");
        member2.setMembershipDate(LocalDate.now());
        member2.setMembershipStatus(
                Member.MembershipStatus.ACTIVE);
        member2.setBorrowRecords(new ArrayList<>());
        entityManager.persist(member2);

        // active borrow record
        activeRecord = new BorrowRecord();
        activeRecord.setBook(book);
        activeRecord.setMember(member1);
        activeRecord.setBorrowDate(LocalDate.now());
        activeRecord.setDueDate(
                LocalDate.now().plusDays(30));
        activeRecord.setStatus(
                BorrowRecord.BorrowStatus.BORROWED);
        activeRecord.setFine(0.0);
        entityManager.persist(activeRecord);

        // returned borrow record
        returnedRecord = new BorrowRecord();
        returnedRecord.setBook(book);
        returnedRecord.setMember(member1);
        returnedRecord.setBorrowDate(
                LocalDate.now().minusDays(20));
        returnedRecord.setDueDate(
                LocalDate.now().minusDays(10));
        returnedRecord.setReturnDate(
                LocalDate.now().minusDays(12));
        returnedRecord.setStatus(
                BorrowRecord.BorrowStatus.RETURNED);
        returnedRecord.setFine(0.0);
        entityManager.persist(returnedRecord);

        // overdue borrow record
        overdueRecord = new BorrowRecord();
        overdueRecord.setBook(book);
        overdueRecord.setMember(member2);
        overdueRecord.setBorrowDate(
                LocalDate.now().minusDays(20));
        overdueRecord.setDueDate(
                LocalDate.now().minusDays(5));  // past due!
        overdueRecord.setStatus(
                BorrowRecord.BorrowStatus.BORROWED);
        overdueRecord.setFine(0.0);
        entityManager.persist(overdueRecord);

        entityManager.flush();
    }

    // ══════════════════════════════════════════════
    // TEST 1 — findByMemberId returns records
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 1: findByMemberId should return borrow records")
    void findByMemberId_ShouldReturnRecords() {

        List<BorrowRecord> result = borrowRepository
                .findByMemberId(member1.getId());

        // member1 has 2 records (active + returned)
        assertEquals(2, result.size());
    }

    // ══════════════════════════════════════════════
    // TEST 2 — findByBookId returns records
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 2: findByBookId should return records for book")
    void findByBookId_ShouldReturnRecords() {

        List<BorrowRecord> result = borrowRepository
                .findByBookId(book.getId());

        // all 3 records are for the same book
        assertEquals(3, result.size());
    }

    // ══════════════════════════════════════════════
    // TEST 3 — findByStatus returns correct records
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 3: findByStatus should return records by status")
    void findByStatus_ShouldReturnCorrectRecords() {

        // BORROWED — activeRecord + overdueRecord = 2
        List<BorrowRecord> borrowed = borrowRepository
                .findByStatus(BorrowRecord.BorrowStatus.BORROWED);
        assertEquals(2, borrowed.size());

        // RETURNED — returnedRecord = 1
        List<BorrowRecord> returned = borrowRepository
                .findByStatus(BorrowRecord.BorrowStatus.RETURNED);
        assertEquals(1, returned.size());
    }

    // ══════════════════════════════════════════════
    // TEST 4 — findOverdueRecords returns overdue
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 4: findOverdueRecords should return overdue records")
    void findOverdueRecords_ShouldReturnOverdue() {

        List<BorrowRecord> overdue = borrowRepository
                .findOverdueRecords(LocalDate.now());

        // only overdueRecord is past due date
        assertEquals(1, overdue.size());
        assertEquals(member2.getId(),
                overdue.get(0).getMember().getId());
    }

    // ══════════════════════════════════════════════
    // TEST 5 — existsByBookIdAndMemberIdAndStatus
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 5: existsByBookIdAndMemberIdAndStatus should return correct boolean")
    void existsByBookIdAndMemberIdAndStatus_ShouldReturnBoolean() {

        // member1 has an active borrow for this book
        assertTrue(borrowRepository
                .existsByBookIdAndMemberIdAndStatus(
                        book.getId(),
                        member1.getId(),
                        BorrowRecord.BorrowStatus.BORROWED));

        // member1 does NOT have OVERDUE status
        assertFalse(borrowRepository
                .existsByBookIdAndMemberIdAndStatus(
                        book.getId(),
                        member1.getId(),
                        BorrowRecord.BorrowStatus.OVERDUE));
    }

    // ══════════════════════════════════════════════
    // TEST 6 — findByIdWithDetails returns record
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 6: findByIdWithDetails should return record with book and member")
    void findByIdWithDetails_ShouldReturnRecord() {

        Optional<BorrowRecord> result = borrowRepository
                .findByIdWithDetails(activeRecord.getId());

        assertTrue(result.isPresent());
        assertNotNull(result.get().getBook());
        assertNotNull(result.get().getMember());
        assertEquals("Effective Java",
                result.get().getBook().getTitle());
        assertEquals("Akram",
                result.get().getMember().getFirstname());
    }

    // ══════════════════════════════════════════════
    // TEST 7 — findAllWithDetails paged
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 7: findAllWithDetails should return paged records")
    void findAllWithDetails_ShouldReturnPagedRecords() {

        Pageable pageable = PageRequest.of(0, 10);
        Page<BorrowRecord> result = borrowRepository
                .findAllWithDetails(pageable);

        assertEquals(3, result.getTotalElements());
        assertFalse(result.getContent().isEmpty());
    }

    // ══════════════════════════════════════════════
    // TEST 8 — markOverdueRecords updates status
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 8: markOverdueRecords should update status to OVERDUE")
    void markOverdueRecords_ShouldUpdateStatus() {

        int updated = borrowRepository
                .markOverdueRecords(LocalDate.now());

        // overdueRecord should be marked overdue
        assertEquals(1, updated);

        entityManager.flush();
        entityManager.clear();

        // verify status changed
        BorrowRecord updated1 = borrowRepository
                .findById(overdueRecord.getId()).orElse(null);

        assertNotNull(updated1);
        assertEquals(BorrowRecord.BorrowStatus.OVERDUE,
                updated1.getStatus());
    }

    // ══════════════════════════════════════════════
    // TEST 9 — countByStatus returns correct count
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 9: countByStatus should return correct count")
    void countByStatus_ShouldReturnCorrectCount() {

        Long borrowed = borrowRepository
                .countByStatus(BorrowRecord.BorrowStatus.BORROWED);
        assertEquals(2L, borrowed);

        Long returned = borrowRepository
                .countByStatus(BorrowRecord.BorrowStatus.RETURNED);
        assertEquals(1L, returned);

        Long overdue = borrowRepository
                .countByStatus(BorrowRecord.BorrowStatus.OVERDUE);
        assertEquals(0L, overdue);
    }

    // ══════════════════════════════════════════════
    // TEST 10 — findByMemberIdWithDetails
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 10: findByMemberIdWithDetails should return records with details")
    void findByMemberIdWithDetails_ShouldReturnRecords() {

        List<BorrowRecord> result = borrowRepository
                .findByMemberIdWithDetails(member1.getId());

        // member1 has 2 records
        assertEquals(2, result.size());

        // each record should have book details
        result.forEach(record -> {
            assertNotNull(record.getBook());
            assertEquals("Effective Java",
                    record.getBook().getTitle());
        });
    }
}