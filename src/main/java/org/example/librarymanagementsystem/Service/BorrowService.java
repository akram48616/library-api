package org.example.librarymanagementsystem.Service;

import lombok.extern.slf4j.Slf4j;
import org.example.librarymanagementsystem.DTO.RequestDTO.*;
import org.example.librarymanagementsystem.DTO.ResponseDTO.*;
import org.example.librarymanagementsystem.Repository.BookRepository;
import org.example.librarymanagementsystem.Repository.BorrowRecordRepository;
import org.example.librarymanagementsystem.Repository.MemberRepository;
import org.example.librarymanagementsystem.entity.Book;
import org.example.librarymanagementsystem.entity.BorrowRecord;
import org.example.librarymanagementsystem.entity.*;
import org.example.librarymanagementsystem.exception.BookNotAvailableException;
import org.example.librarymanagementsystem.exception.BookNotFoundException;
import org.example.librarymanagementsystem.exception.BorrowRecordNotFoundException;
import org.example.librarymanagementsystem.exception.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BorrowService {

    @Autowired
    private BorrowRecordRepository borrowRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ModelMapper modelMapper;

    private static final double FINE_PER_DAY = 5.0;

    private BorrowResponseDTO toDTO(BorrowRecord record) {
        BorrowResponseDTO dto = new BorrowResponseDTO();
        dto.setId(record.getId());
        dto.setBorrowDate(record.getBorrowDate());
        dto.setReturnDate(record.getReturnDate());
        dto.setDueDate(record.getDueDate());
        dto.setFine(record.getFine());
        dto.setStatus(record.getStatus());

        dto.setOverdue(
                record.getDueDate() != null
                        && record.getDueDate().isBefore(LocalDate.now())
                        && record.getStatus() != BorrowRecord.BorrowStatus.RETURNED
        );

        if (record.getBook() != null) {
            BookSummaryDTO bookDTO = BookSummaryDTO.builder()
                    .id(record.getBook().getId())
                    .title(record.getBook().getTitle())
                    .stock(record.getBook().getStock())
                    .build();
            dto.setBook(bookDTO);
        }

        if (record.getMember() != null) {
            MemberSummaryDTO memberDTO = MemberSummaryDTO.builder()
                    .id(record.getMember().getId())
                    .firstname(record.getMember().getFirstname())
                    .lastname(record.getMember().getLastname())
                    .build();
            dto.setMember(memberDTO);
        }

        return dto;
    }

    private PageResponseDTO<BorrowResponseDTO> toPageResponse(
            Page<BorrowRecord> page) {

        List<BorrowResponseDTO> content = page.getContent()
                .stream().map(this::toDTO).collect(Collectors.toList());

        return PageResponseDTO.<BorrowResponseDTO>builder()
                .content(content)
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .pageSize(page.getSize())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }

    // ─── GET all paged ─────────────────────────────
    @Cacheable(
            value = "borrows",
            key = "'all:' + #page + ':' + #size + ':'" +
                    "+ #sortBy + ':' + #direction"
    )
    @Transactional(readOnly = true)
    public PageResponseDTO<BorrowResponseDTO> getAllBorrows(
            int page, int size,
            String sortBy, String direction) {
        log.info("Fetching all borrows - page={}, size={}, sortBy={}, direction={}", page, size, sortBy, direction);

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return toPageResponse(
                borrowRepository.findAllWithDetails(pageable));
    }

    // ─── GET by id ─────────────────────────────────
    @Cacheable(value = "borrow", key = "#id")
    @Transactional(readOnly = true)
    public BorrowResponseDTO getById(Integer id) {
        log.info("Fetching borrow record by id={}", id);
        BorrowRecord record = borrowRepository
                .findByIdWithDetails(id)
                .orElseThrow(() -> {
                    log.warn("Borrow record not found for id={}", id);
                    return new BorrowRecordNotFoundException(id);
                });
        return toDTO(record);
    }

    // ─── GET overdue borrows ───────────────────────
    @Cacheable(value = "borrows", key = "'overdue'")
    @Transactional(readOnly = true)
    public List<BorrowResponseDTO> getOverdueBorrows() {
        log.info("Fetching overdue borrow records");
        List<BorrowResponseDTO> overdue = borrowRepository
                .findOverdueRecords(LocalDate.now())
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
        log.debug("Found {} overdue records", overdue.size());
        return overdue;
    }

    // ─── GET by member ─────────────────────────────
    @Cacheable(
            value = "borrows",
            key = "'member:' + #memberId"
    )
    @Transactional(readOnly = true)
    public List<BorrowResponseDTO> getByMember(Integer memberId) {
        log.info("Fetching borrows by memberId={}", memberId);

        if (!memberRepository.existsById(memberId)) {
            log.warn("Member not found for id={}", memberId);
            throw new MemberNotFoundException(memberId);
        }

        List<BorrowResponseDTO> borrows = borrowRepository
                .findByMemberIdWithDetails(memberId)
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
        log.debug("Found {} borrows for memberId={}", borrows.size(), memberId);
        return borrows;
    }

    // ─── GET by book ───────────────────────────────
    @Cacheable(
            value = "borrows",
            key = "'book:' + #bookId"
    )
    @Transactional(readOnly = true)
    public List<BorrowResponseDTO> getByBook(Integer bookId) {
        log.info("Fetching borrows by bookId={}", bookId);

        if (!bookRepository.existsById(bookId)) {
            log.warn("Book not found for id={}", bookId);
            throw new BookNotFoundException(bookId);
        }

        List<BorrowResponseDTO> borrows = borrowRepository
                .findByBookIdWithDetails(bookId)
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
        log.debug("Found {} borrows for bookId={}", borrows.size(), bookId);
        return borrows;
    }

    // ─── BORROW a book ─────────────────────────────
    @CacheEvict(value = "borrows", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public BorrowResponseDTO borrowBook(BorrowRequestDTO dto) {
        log.info("Processing borrow request - bookId={}, memberId={}", dto.getBookId(), dto.getMemberId());

        Book book = bookRepository
                .findByIdWithDetails(dto.getBookId())
                .orElseThrow(() -> {
                    log.warn("Book not found for id={}", dto.getBookId());
                    return new BookNotFoundException(dto.getBookId());
                });

        if (book.getStock() <= 0) {
            log.warn("Book id={} not available (stock=0)", dto.getBookId());
            throw new BookNotAvailableException(dto.getBookId());
        }

        Member member = memberRepository
                .findById(dto.getMemberId())
                .orElseThrow(() -> {
                    log.warn("Member not found for id={}", dto.getMemberId());
                    return new MemberNotFoundException(dto.getMemberId());
                });

        if (member.getMembershipStatus() !=
                Member.MembershipStatus.ACTIVE) {
            log.warn("Member id={} is not active (status={})", dto.getMemberId(), member.getMembershipStatus());
            throw new MemberNotFoundException(dto.getMemberId());
        }

        if (borrowRepository.existsByBookIdAndMemberIdAndStatus(
                dto.getBookId(), dto.getMemberId(),
                BorrowRecord.BorrowStatus.BORROWED)) {
            log.warn("Member id={} already has bookId={} borrowed", dto.getMemberId(), dto.getBookId());
            throw new BadRequestException(
                    "Member already has this book borrowed");
        }

        BorrowRecord record = BorrowRecord.builder()
                .book(book)
                .member(member)
                .borrowDate(LocalDate.now())
                .dueDate(dto.getDueDate())
                .status(BorrowRecord.BorrowStatus.BORROWED)
                .fine(0.0)
                .build();

        BorrowRecord saved = borrowRepository.save(record);

        bookRepository.decreaseStock(dto.getBookId());
        log.info("Borrow successful - borrowId={}, bookId={}, memberId={}", saved.getId(), dto.getBookId(), dto.getMemberId());

        return toDTO(saved);
    }

    // ─── RETURN a book ─────────────────────────────
    @CacheEvict(value = "borrows", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public BorrowResponseDTO returnBook(Integer borrowId) {
        log.info("Processing return for borrowId={}", borrowId);

        BorrowRecord record = borrowRepository
                .findByIdWithDetails(borrowId)
                .orElseThrow(() -> {
                    log.warn("Borrow record not found for id={}", borrowId);
                    return new BorrowRecordNotFoundException(borrowId);
                });

        if (record.getStatus() ==
                BorrowRecord.BorrowStatus.RETURNED) {
            log.warn("Borrow id={} already returned", borrowId);
            throw new AlreadyReturnedException(borrowId);
        }

        record.setReturnDate(LocalDate.now());

        if (LocalDate.now().isAfter(record.getDueDate())) {
            long daysOverdue = ChronoUnit.DAYS.between(
                    record.getDueDate(), LocalDate.now());
            double fine = daysOverdue * FINE_PER_DAY;
            record.setFine(fine);
            record.setStatus(BorrowRecord.BorrowStatus.OVERDUE);
            log.info("Borrow id={} returned late by {} day(s), fine={}", borrowId, daysOverdue, fine);
        }

        record.setStatus(BorrowRecord.BorrowStatus.RETURNED);

        BorrowRecord updated = borrowRepository.save(record);

        bookRepository.increaseStock(
                record.getBook().getId());
        log.info("Return successful - borrowId={}, bookId={}", borrowId, record.getBook().getId());

        return toDTO(updated);
    }

    // ─── UPDATE overdue records ────────────────────
    @CacheEvict(value = "borrows", allEntries = true)
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(rollbackFor = Exception.class)
    public String updateOverdueRecords() {
        log.info("Running batch update for overdue records");
        int updated = borrowRepository
                .markOverdueRecords(LocalDate.now());
        log.info("Marked {} records as overdue", updated);
        return updated + " records marked as overdue";
    }
}