package org.example.librarymanagementsystem.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.librarymanagementsystem.DTO.RequestDTO.BorrowRequestDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.*;
import org.example.librarymanagementsystem.Service.BorrowService;
import org.example.librarymanagementsystem.entity.BorrowRecord;
import org.example.librarymanagementsystem.exception.BorrowRecordNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BorrowController.class)
@ActiveProfiles("webmvc")
class BorrowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BorrowService borrowService;

    @Autowired
    private ObjectMapper objectMapper;

    private BorrowResponseDTO borrowResponseDTO;
    private BorrowRequestDTO borrowRequestDTO;
    private PageResponseDTO<BorrowResponseDTO> pageResponse;

    @BeforeEach
    void setUp() {

        BookSummaryDTO book = BookSummaryDTO.builder()
                .id(1)
                .title("Effective Java")
                .stock(10)
                .build();

        MemberSummaryDTO member = MemberSummaryDTO.builder()
                .id(1)
                .firstname("Akram")
                .lastname("Shaik")
                .build();

        borrowResponseDTO = BorrowResponseDTO.builder()
                .id(1)
                .borrowDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(7))
                .returnDate(null)
                .fine(0)
                .status(BorrowRecord.BorrowStatus.BORROWED)
                .overdue(false)
                .book(book)
                .member(member)
                .build();

        borrowRequestDTO = BorrowRequestDTO.builder()
                .bookId(1)
                .memberId(1)
                .dueDate(LocalDate.now().plusDays(7))
                .build();

        pageResponse = PageResponseDTO.<BorrowResponseDTO>builder()
                .content(List.of(borrowResponseDTO))
                .currentPage(0)
                .pageSize(10)
                .totalPages(1)
                .totalElements(1)
                .first(true)
                .last(true)
                .empty(false)
                .build();
    }

    //══════════════════════════════════════════════
    // TEST 1 GET ALL BORROWS
    //══════════════════════════════════════════════

    @Test
    @DisplayName("GET /api/borrows returns paged borrows")
    void getAll_ShouldReturn200() throws Exception {

        when(borrowService.getAllBorrows(0,10,"borrowDate","desc"))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/borrows"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content",hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(borrowService).getAllBorrows(0,10,"borrowDate","desc");
    }

    //══════════════════════════════════════════════
    // TEST 2 GET BORROW BY ID
    //══════════════════════════════════════════════

    @Test
    @DisplayName("GET borrow by id")
    void getById_ShouldReturn200() throws Exception {

        when(borrowService.getById(1))
                .thenReturn(borrowResponseDTO);

        mockMvc.perform(get("/api/borrows/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.book.title")
                        .value("Effective Java"))
                .andExpect(jsonPath("$.member.firstname")
                        .value("Akram"));

        verify(borrowService).getById(1);
    }

    //══════════════════════════════════════════════
    // TEST 3 BORROW NOT FOUND
    //══════════════════════════════════════════════

    @Test
    @DisplayName("GET borrow returns 404")
    void getById_ShouldReturn404() throws Exception {

        when(borrowService.getById(999))
                .thenThrow(new BorrowRecordNotFoundException(999));

        mockMvc.perform(get("/api/borrows/999"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Borrow record with id 999 not found"));

        verify(borrowService).getById(999);
    }

    //══════════════════════════════════════════════
    // TEST 4 GET OVERDUE BORROWS
    //══════════════════════════════════════════════

    @Test
    @DisplayName("GET overdue borrows")
    void getOverdue_ShouldReturn200() throws Exception {

        when(borrowService.getOverdueBorrows())
                .thenReturn(List.of(borrowResponseDTO));

        mockMvc.perform(get("/api/borrows/overdue"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$",hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1));

        verify(borrowService).getOverdueBorrows();
    }
    //══════════════════════════════════════════════
    // TEST 5 GET BORROWS BY MEMBER
    //══════════════════════════════════════════════

    @Test
    @DisplayName("GET borrows by member")
    void getByMember_ShouldReturn200() throws Exception {

        when(borrowService.getByMember(1))
                .thenReturn(List.of(borrowResponseDTO));

        mockMvc.perform(get("/api/borrows/member/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].member.firstname")
                        .value("Akram"));

        verify(borrowService).getByMember(1);
    }

    //══════════════════════════════════════════════
    // TEST 6 GET BORROWS BY BOOK
    //══════════════════════════════════════════════

    @Test
    @DisplayName("GET borrows by book")
    void getByBook_ShouldReturn200() throws Exception {

        when(borrowService.getByBook(1))
                .thenReturn(List.of(borrowResponseDTO));

        mockMvc.perform(get("/api/borrows/book/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].book.title")
                        .value("Effective Java"));

        verify(borrowService).getByBook(1);
    }

    //══════════════════════════════════════════════
    // TEST 7 BORROW BOOK SUCCESS
    //══════════════════════════════════════════════

    @Test
    @DisplayName("POST borrow book")
    void borrowBook_ShouldReturn200() throws Exception {

        when(borrowService.borrowBook(any(BorrowRequestDTO.class)))
                .thenReturn(borrowResponseDTO);

        mockMvc.perform(post("/api/borrows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrowRequestDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.book.title")
                        .value("Effective Java"))
                .andExpect(jsonPath("$.member.firstname")
                        .value("Akram"));

        verify(borrowService).borrowBook(any(BorrowRequestDTO.class));
    }

    //══════════════════════════════════════════════
    // TEST 8 VALIDATION FAILURE
    //══════════════════════════════════════════════

    @Test
    @DisplayName("POST invalid borrow request returns 400")
    void borrowBook_ShouldReturn400_WhenValidationFails() throws Exception {

        BorrowRequestDTO invalid = new BorrowRequestDTO();

        mockMvc.perform(post("/api/borrows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("Validation Failed"));

        verify(borrowService, never())
                .borrowBook(any(BorrowRequestDTO.class));
    }
    //══════════════════════════════════════════════
    // TEST 9 RETURN BOOK SUCCESS
    //══════════════════════════════════════════════

    @Test
    @DisplayName("PUT /api/borrows/{id}/return returns updated borrow")
    void returnBook_ShouldReturn200() throws Exception {

        BorrowResponseDTO returnedBorrow = BorrowResponseDTO.builder()
                .id(1)
                .borrowDate(LocalDate.now().minusDays(5))
                .dueDate(LocalDate.now().plusDays(2))
                .returnDate(LocalDate.now())
                .fine(0.0)
                .status(BorrowRecord.BorrowStatus.RETURNED)
                .overdue(false)
                .book(borrowResponseDTO.getBook())
                .member(borrowResponseDTO.getMember())
                .build();

        when(borrowService.returnBook(1))
                .thenReturn(returnedBorrow);

        mockMvc.perform(put("/api/borrows/1/return"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("RETURNED"))
                .andExpect(jsonPath("$.overdue").value(false));

        verify(borrowService).returnBook(1);
    }

    //══════════════════════════════════════════════
    // TEST 10 RETURN BOOK NOT FOUND
    //══════════════════════════════════════════════

    @Test
    @DisplayName("PUT return book should return 404")
    void returnBook_ShouldReturn404() throws Exception {

        when(borrowService.returnBook(999))
                .thenThrow(new BorrowRecordNotFoundException(999));

        mockMvc.perform(put("/api/borrows/999/return"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Borrow record with id 999 not found"));

        verify(borrowService).returnBook(999);
    }

    //══════════════════════════════════════════════
    // TEST 11 UPDATE OVERDUE RECORDS
    //══════════════════════════════════════════════

    @Test
    @DisplayName("PUT /api/borrows/update-overdue returns success message")
    void updateOverdue_ShouldReturn200() throws Exception {

        when(borrowService.updateOverdueRecords())
                .thenReturn("Overdue records updated successfully");

        mockMvc.perform(put("/api/borrows/update-overdue"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content()
                        .string("Overdue records updated successfully"));

        verify(borrowService).updateOverdueRecords();
    }

}