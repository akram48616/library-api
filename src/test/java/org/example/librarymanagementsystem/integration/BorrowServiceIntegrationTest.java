package org.example.librarymanagementsystem.integration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.librarymanagementsystem.DTO.RequestDTO.BorrowRequestDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.BorrowResponseDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.PageResponseDTO;
import org.example.librarymanagementsystem.Repository.BookRepository;
import org.example.librarymanagementsystem.Repository.BorrowRecordRepository;
import org.example.librarymanagementsystem.Repository.MemberRepository;
import org.example.librarymanagementsystem.Service.BorrowService;
import org.example.librarymanagementsystem.entity.Book;
import org.example.librarymanagementsystem.entity.BorrowRecord;
import org.example.librarymanagementsystem.entity.Member;
import org.example.librarymanagementsystem.exception.AlreadyReturnedException;
import org.example.librarymanagementsystem.exception.BookNotAvailableException;
import org.example.librarymanagementsystem.exception.BorrowRecordNotFoundException;
import org.example.librarymanagementsystem.exception.MemberNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BorrowServiceIntegrationTest {

    @Autowired
    private BorrowService borrowService;

    @Autowired
    private BorrowRecordRepository borrowRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Book book;
    private Member member;

    @BeforeEach
    void setUp() {

        book = new Book();
        book.setTitle("Effective Java");
        book.setIsbn("9780134685991");
        book.setPrice(599.0);
        book.setStock(10);
        book.setPublishedDate(LocalDate.of(2018, 1, 6));

        book = bookRepository.save(book);

        member = new Member();
        member.setFirstname("John");
        member.setLastname("Smith");
        member.setEmail("john" + System.nanoTime() + "@test.com");
        member.setMembershipStatus(Member.MembershipStatus.ACTIVE);

        member = memberRepository.save(member);

        entityManager.flush();
        entityManager.clear();
    }

    // =====================================================
    // TEST 1
    // =====================================================

    @Test
    @DisplayName("Test 1: borrowBook should save borrow record")
    void borrowBook_ShouldSaveBorrowRecord() {

        BorrowRequestDTO request = new BorrowRequestDTO();
        request.setBookId(book.getId());
        request.setMemberId(member.getId());
        request.setDueDate(LocalDate.now().plusDays(14));

        BorrowResponseDTO result = borrowService.borrowBook(request);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(BorrowRecord.BorrowStatus.BORROWED, result.getStatus());
        assertEquals("Effective Java", result.getBook().getTitle());
        assertEquals("John", result.getMember().getFirstname());
        assertEquals(0.0, result.getFine());
        assertFalse(result.isOverdue());
        assertEquals(1, borrowRepository.count());
    }

    // =====================================================
    // TEST 2
    // =====================================================

    @Test
    @DisplayName("Test 2: borrowBook should decrease book stock")
    void borrowBook_ShouldDecreaseStock() {

        int originalStock = book.getStock();

        BorrowRequestDTO request = new BorrowRequestDTO();
        request.setBookId(book.getId());
        request.setMemberId(member.getId());
        request.setDueDate(LocalDate.now().plusDays(14));

        borrowService.borrowBook(request);

        entityManager.flush();
        entityManager.clear();

        Book updatedBook = bookRepository.findById(book.getId()).orElseThrow();

        assertEquals(originalStock - 1, updatedBook.getStock());
    }

    // =====================================================
    // TEST 3
    // =====================================================

    @Test
    @DisplayName("Test 3: borrowBook should throw when book is unavailable")
    void borrowBook_ShouldThrow_WhenBookUnavailable() {

        book.setStock(0);
        bookRepository.save(book);
        entityManager.flush();
        entityManager.clear();

        BorrowRequestDTO request = new BorrowRequestDTO();
        request.setBookId(book.getId());
        request.setMemberId(member.getId());
        request.setDueDate(LocalDate.now().plusDays(14));

        assertThrows(
                BookNotAvailableException.class,
                () -> borrowService.borrowBook(request)
        );

        assertEquals(0, borrowRepository.count());
    }

    // =====================================================
    // TEST 4
    // =====================================================

    @Test
    @DisplayName("Test 4: borrowBook should throw when member does not exist")
    void borrowBook_ShouldThrow_WhenMemberNotFound() {

        BorrowRequestDTO request = new BorrowRequestDTO();
        request.setBookId(book.getId());
        request.setMemberId(99999);
        request.setDueDate(LocalDate.now().plusDays(14));

        assertThrows(
                MemberNotFoundException.class,
                () -> borrowService.borrowBook(request)
        );

        assertEquals(0, borrowRepository.count());
    }

    // =====================================================
    // TEST 5
    // =====================================================

    @Test
    @DisplayName("Test 5: getById should return borrow record")
    void getById_ShouldReturnBorrowRecord() {

        BorrowRequestDTO request = new BorrowRequestDTO();
        request.setBookId(book.getId());
        request.setMemberId(member.getId());
        request.setDueDate(LocalDate.now().plusDays(14));

        BorrowResponseDTO saved = borrowService.borrowBook(request);

        BorrowResponseDTO result = borrowService.getById(saved.getId());

        assertNotNull(result);
        assertEquals(saved.getId(), result.getId());
        assertEquals("Effective Java", result.getBook().getTitle());
        assertEquals("John", result.getMember().getFirstname());
        assertEquals(BorrowRecord.BorrowStatus.BORROWED, result.getStatus());
    }

    // =====================================================
    // TEST 6
    // =====================================================

    @Test
    @DisplayName("Test 6: getById should throw when borrow record not found")
    void getById_ShouldThrow_WhenNotFound() {

        assertThrows(
                BorrowRecordNotFoundException.class,
                () -> borrowService.getById(99999)
        );
    }

    // =====================================================
    // TEST 7
    // =====================================================

    @Test
    @DisplayName("Test 7: returnBook should return book and increase stock")
    void returnBook_ShouldReturnBookAndIncreaseStock() {

        BorrowRequestDTO request = new BorrowRequestDTO();
        request.setBookId(book.getId());
        request.setMemberId(member.getId());
        request.setDueDate(LocalDate.now().plusDays(14));

        BorrowResponseDTO borrowed = borrowService.borrowBook(request);

        entityManager.flush();
        entityManager.clear();

        int stockAfterBorrow =
                bookRepository.findById(book.getId())
                        .orElseThrow()
                        .getStock();

        BorrowResponseDTO returned = borrowService.returnBook(borrowed.getId());

        assertNotNull(returned);
        assertEquals(BorrowRecord.BorrowStatus.RETURNED, returned.getStatus());
        assertNotNull(returned.getReturnDate());
        assertEquals(0.0, returned.getFine());

        entityManager.flush();
        entityManager.clear();

        int stockAfterReturn =
                bookRepository.findById(book.getId())
                        .orElseThrow()
                        .getStock();

        assertEquals(stockAfterBorrow + 1, stockAfterReturn);
    }

    // =====================================================
    // TEST 8
    // =====================================================

    @Test
    @DisplayName("Test 8: returnBook should throw when already returned")
    void returnBook_ShouldThrow_WhenAlreadyReturned() {

        BorrowRequestDTO request = new BorrowRequestDTO();
        request.setBookId(book.getId());
        request.setMemberId(member.getId());
        request.setDueDate(LocalDate.now().plusDays(14));

        BorrowResponseDTO borrowed = borrowService.borrowBook(request);

        borrowService.returnBook(borrowed.getId());

        assertThrows(
                AlreadyReturnedException.class,
                () -> borrowService.returnBook(borrowed.getId())
        );
    }

    // =====================================================
    // TEST 9
    // =====================================================

    @Test
    @DisplayName("Test 9: getByMember should return member borrow history")
    void getByMember_ShouldReturnBorrowHistory() {

        BorrowRequestDTO request = new BorrowRequestDTO();
        request.setBookId(book.getId());
        request.setMemberId(member.getId());
        request.setDueDate(LocalDate.now().plusDays(14));

        borrowService.borrowBook(request);

        List<BorrowResponseDTO> result =
                borrowService.getByMember(member.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Effective Java", result.get(0).getBook().getTitle());
        assertEquals("John", result.get(0).getMember().getFirstname());
    }

    // =====================================================
    // TEST 10
    // =====================================================

    @Test
    @DisplayName("Test 10: getAllBorrows should return paginated records")
    void getAllBorrows_ShouldReturnPagedRecords() {

        BorrowRequestDTO request = new BorrowRequestDTO();
        request.setBookId(book.getId());
        request.setMemberId(member.getId());
        request.setDueDate(LocalDate.now().plusDays(14));

        borrowService.borrowBook(request);

        PageResponseDTO<BorrowResponseDTO> result =
                borrowService.getAllBorrows(0, 10, "borrowDate", "asc");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(0, result.getCurrentPage());
        assertEquals(10, result.getPageSize());
        assertFalse(result.isEmpty());
        assertEquals(1, result.getContent().size());
        assertEquals("Effective Java", result.getContent().get(0).getBook().getTitle());
    }
}