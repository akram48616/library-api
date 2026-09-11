package org.example.librarymanagementsystem.Service;

import org.example.librarymanagementsystem.DTO.RequestDTO.BorrowRequestDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.BorrowResponseDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.PageResponseDTO;
import org.example.librarymanagementsystem.Repository.BookRepository;
import org.example.librarymanagementsystem.Repository.BorrowRecordRepository;
import org.example.librarymanagementsystem.Repository.MemberRepository;
import org.example.librarymanagementsystem.entity.Book;
import org.example.librarymanagementsystem.entity.BorrowRecord;
import org.example.librarymanagementsystem.entity.Member;
import org.example.librarymanagementsystem.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowServiceTest {

    @Mock
    private BorrowRecordRepository borrowRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private BorrowService borrowService;

    private Book book;
    private Member member;
    private BorrowRecord borrowRecord;

    @BeforeEach
    void setUp() {
        book = new Book();
        book.setId(1);
        book.setTitle("Effective Java");
        book.setStock(3);

        member = new Member();
        member.setId(10);
        member.setFirstname("Shabana");
        member.setLastname("Shaik");
        member.setMembershipStatus(Member.MembershipStatus.ACTIVE);

        borrowRecord = BorrowRecord.builder()
                .id(100)
                .book(book)
                .member(member)
                .borrowDate(LocalDate.now().minusDays(5))
                .dueDate(LocalDate.now().plusDays(9))
                .status(BorrowRecord.BorrowStatus.BORROWED)
                .fine(0.0)
                .build();
    }

    // ───────────────────────── getAllBorrows ─────────────────────────

    @Test
    void getAllBorrows_returnsMappedPageResponse() {
        Page<BorrowRecord> page = new PageImpl<>(List.of(borrowRecord));
        when(borrowRepository.findAllWithDetails(any(Pageable.class))).thenReturn(page);

        PageResponseDTO<BorrowResponseDTO> result =
                borrowService.getAllBorrows(0, 10, "id", "asc");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(borrowRecord.getId(), result.getContent().get(0).getId());
        verify(borrowRepository).findAllWithDetails(any(Pageable.class));
    }

    // ───────────────────────── getById ─────────────────────────

    @Test
    void getById_whenFound_returnsDto() {
        when(borrowRepository.findByIdWithDetails(100))
                .thenReturn(Optional.of(borrowRecord));

        BorrowResponseDTO dto = borrowService.getById(100);

        assertEquals(100, dto.getId());
        assertEquals(1, dto.getBook().getId());
        assertEquals(10, dto.getMember().getId());
    }

    @Test
    void getById_whenNotFound_throwsBorrowRecordNotFoundException() {
        when(borrowRepository.findByIdWithDetails(999))
                .thenReturn(Optional.empty());

        assertThrows(BorrowRecordNotFoundException.class,
                () -> borrowService.getById(999));
    }

    // ───────────────────────── getOverdueBorrows ─────────────────────────

    @Test
    void getOverdueBorrows_returnsMappedList() {
        BorrowRecord overdue = BorrowRecord.builder()
                .id(101)
                .book(book)
                .member(member)
                .borrowDate(LocalDate.now().minusDays(20))
                .dueDate(LocalDate.now().minusDays(5))
                .status(BorrowRecord.BorrowStatus.BORROWED)
                .fine(0.0)
                .build();

        when(borrowRepository.findOverdueRecords(any(LocalDate.class)))
                .thenReturn(List.of(overdue));

        List<BorrowResponseDTO> result = borrowService.getOverdueBorrows();

        assertEquals(1, result.size());
        assertTrue(result.get(0).isOverdue());
    }

    // ───────────────────────── getByMember ─────────────────────────

    @Test
    void getByMember_whenMemberExists_returnsList() {
        when(memberRepository.existsById(10)).thenReturn(true);
        when(borrowRepository.findByMemberIdWithDetails(10))
                .thenReturn(List.of(borrowRecord));

        List<BorrowResponseDTO> result = borrowService.getByMember(10);

        assertEquals(1, result.size());
        verify(borrowRepository).findByMemberIdWithDetails(10);
    }

    @Test
    void getByMember_whenMemberMissing_throwsMemberNotFoundException() {
        when(memberRepository.existsById(999)).thenReturn(false);

        assertThrows(MemberNotFoundException.class,
                () -> borrowService.getByMember(999));
        verifyNoInteractions(borrowRepository);
    }

    // ───────────────────────── getByBook ─────────────────────────

    @Test
    void getByBook_whenBookExists_returnsList() {
        when(bookRepository.existsById(1)).thenReturn(true);
        when(borrowRepository.findByBookIdWithDetails(1))
                .thenReturn(List.of(borrowRecord));

        List<BorrowResponseDTO> result = borrowService.getByBook(1);

        assertEquals(1, result.size());
    }

    @Test
    void getByBook_whenBookMissing_throwsBookNotFoundException() {
        when(bookRepository.existsById(999)).thenReturn(false);

        assertThrows(BookNotFoundException.class,
                () -> borrowService.getByBook(999));
        verifyNoInteractions(borrowRepository);
    }

    // ───────────────────────── borrowBook ─────────────────────────

    @Test
    void borrowBook_success_createsRecordAndDecreasesStock() {
        BorrowRequestDTO request = new BorrowRequestDTO();
        request.setBookId(1);
        request.setMemberId(10);
        request.setDueDate(LocalDate.now().plusDays(14));

        when(bookRepository.findByIdWithDetails(1)).thenReturn(Optional.of(book));
        when(memberRepository.findById(10)).thenReturn(Optional.of(member));
        when(borrowRepository.existsByBookIdAndMemberIdAndStatus(
                1, 10, BorrowRecord.BorrowStatus.BORROWED)).thenReturn(false);
        when(borrowRepository.save(any(BorrowRecord.class))).thenAnswer(inv -> {
            BorrowRecord r = inv.getArgument(0);
            r.setId(200);
            return r;
        });

        BorrowResponseDTO result = borrowService.borrowBook(request);

        assertNotNull(result);
        assertEquals(200, result.getId());
        assertEquals(BorrowRecord.BorrowStatus.BORROWED, result.getStatus());
        verify(bookRepository).decreaseStock(1);
        verify(borrowRepository).save(any(BorrowRecord.class));
    }

    @Test
    void borrowBook_whenBookNotFound_throwsBookNotFoundException() {
        BorrowRequestDTO request = new BorrowRequestDTO();
        request.setBookId(999);
        request.setMemberId(10);

        when(bookRepository.findByIdWithDetails(999)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class,
                () -> borrowService.borrowBook(request));
        verify(borrowRepository, never()).save(any());
    }

    @Test
    void borrowBook_whenNoStock_throwsBookNotAvailableException() {
        book.setStock(0);
        BorrowRequestDTO request = new BorrowRequestDTO();
        request.setBookId(1);
        request.setMemberId(10);

        when(bookRepository.findByIdWithDetails(1)).thenReturn(Optional.of(book));

        assertThrows(BookNotAvailableException.class,
                () -> borrowService.borrowBook(request));
        verify(memberRepository, never()).findById(any());
    }

    @Test
    void borrowBook_whenMemberNotFound_throwsMemberNotFoundException() {
        BorrowRequestDTO request = new BorrowRequestDTO();
        request.setBookId(1);
        request.setMemberId(999);

        when(bookRepository.findByIdWithDetails(1)).thenReturn(Optional.of(book));
        when(memberRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(MemberNotFoundException.class,
                () -> borrowService.borrowBook(request));
    }

    @Test
    void borrowBook_whenMemberNotActive_throwsMemberNotFoundException() {
        member.setMembershipStatus(Member.MembershipStatus.INACTIVE);
        BorrowRequestDTO request = new BorrowRequestDTO();
        request.setBookId(1);
        request.setMemberId(10);

        when(bookRepository.findByIdWithDetails(1)).thenReturn(Optional.of(book));
        when(memberRepository.findById(10)).thenReturn(Optional.of(member));

        assertThrows(MemberNotFoundException.class,
                () -> borrowService.borrowBook(request));
        verify(borrowRepository, never()).save(any());
    }

    @Test
    void borrowBook_whenAlreadyBorrowed_throwsBadRequestException() {
        BorrowRequestDTO request = new BorrowRequestDTO();
        request.setBookId(1);
        request.setMemberId(10);

        when(bookRepository.findByIdWithDetails(1)).thenReturn(Optional.of(book));
        when(memberRepository.findById(10)).thenReturn(Optional.of(member));
        when(borrowRepository.existsByBookIdAndMemberIdAndStatus(
                1, 10, BorrowRecord.BorrowStatus.BORROWED)).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> borrowService.borrowBook(request));
        verify(borrowRepository, never()).save(any());
    }

    // ───────────────────────── returnBook ─────────────────────────

    @Test
    void returnBook_onTime_returnsWithoutFine() {
        borrowRecord.setDueDate(LocalDate.now().plusDays(2)); // not overdue
        when(borrowRepository.findByIdWithDetails(100)).thenReturn(Optional.of(borrowRecord));
        when(borrowRepository.save(any(BorrowRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        BorrowResponseDTO result = borrowService.returnBook(100);

        assertEquals(BorrowRecord.BorrowStatus.RETURNED, result.getStatus());
        assertEquals(0.0, result.getFine());
        verify(bookRepository).increaseStock(1);
    }

    @Test
    void returnBook_whenOverdue_calculatesFineAndReturns() {
        borrowRecord.setDueDate(LocalDate.now().minusDays(3)); // 3 days overdue
        when(borrowRepository.findByIdWithDetails(100)).thenReturn(Optional.of(borrowRecord));
        when(borrowRepository.save(any(BorrowRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        BorrowResponseDTO result = borrowService.returnBook(100);

        // Fine = 3 days * 5.0/day
        assertEquals(15.0, result.getFine());
        // Status ends up RETURNED even though it was briefly marked OVERDUE
        assertEquals(BorrowRecord.BorrowStatus.RETURNED, result.getStatus());
        verify(bookRepository).increaseStock(1);
    }

    @Test
    void returnBook_whenAlreadyReturned_throwsAlreadyReturnedException() {
        borrowRecord.setStatus(BorrowRecord.BorrowStatus.RETURNED);
        when(borrowRepository.findByIdWithDetails(100)).thenReturn(Optional.of(borrowRecord));

        assertThrows(AlreadyReturnedException.class,
                () -> borrowService.returnBook(100));
        verify(borrowRepository, never()).save(any());
        verify(bookRepository, never()).increaseStock(anyInt());
    }

    @Test
    void returnBook_whenRecordNotFound_throwsBorrowRecordNotFoundException() {
        when(borrowRepository.findByIdWithDetails(999)).thenReturn(Optional.empty());

        assertThrows(BorrowRecordNotFoundException.class,
                () -> borrowService.returnBook(999));
    }

    // ───────────────────────── updateOverdueRecords ─────────────────────────

    @Test
    void updateOverdueRecords_returnsCountMessage() {
        when(borrowRepository.markOverdueRecords(any(LocalDate.class))).thenReturn(4);

        String result = borrowService.updateOverdueRecords();

        assertEquals("4 records marked as overdue", result);
    }
}