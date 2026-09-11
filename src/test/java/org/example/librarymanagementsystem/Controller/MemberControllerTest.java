package org.example.librarymanagementsystem.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.librarymanagementsystem.DTO.RequestDTO.MemberRequestDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.MemberResponseDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.PageResponseDTO;
import org.example.librarymanagementsystem.Service.MemberService;
import org.example.librarymanagementsystem.entity.Member;
import org.example.librarymanagementsystem.exception.MemberNotFoundException;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
@ActiveProfiles("webmvc")
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @Autowired
    private ObjectMapper objectMapper;

    private MemberResponseDTO memberResponseDTO;
    private MemberRequestDTO memberRequestDTO;
    private PageResponseDTO<MemberResponseDTO> pageResponse;

    @BeforeEach
    void setUp() {

        memberResponseDTO = MemberResponseDTO.builder()
                .id(1)
                .firstname("Akram")
                .lastname("Shaik")
                .email("akram@gmail.com")
                .phone("9876543210")
                .memberShipDate(LocalDate.now())
                .membershipStatus(Member.MembershipStatus.ACTIVE)
                .build();

        memberRequestDTO = MemberRequestDTO.builder()
                .firstname("Akram")
                .lastname("Shaik")
                .email("akram@gmail.com")
                .phone("9876543210")
                .build();

        pageResponse = PageResponseDTO.<MemberResponseDTO>builder()
                .content(List.of(memberResponseDTO))
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
    // TEST 1 GET ALL MEMBERS
    //══════════════════════════════════════════════

    @Test
    @DisplayName("GET /api/members returns paged members")
    void getAll_ShouldReturn200() throws Exception {

        when(memberService.getAllMembers(0,10,"firstname","asc"))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/members"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content",hasSize(1)))
                .andExpect(jsonPath("$.content[0].firstname").value("Akram"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(memberService,times(1))
                .getAllMembers(0,10,"firstname","asc");
    }

    //══════════════════════════════════════════════
    // TEST 2 GET MEMBER BY ID
    //══════════════════════════════════════════════

    @Test
    @DisplayName("GET member by id")
    void getById_ShouldReturn200() throws Exception {

        when(memberService.getById(1))
                .thenReturn(memberResponseDTO);

        mockMvc.perform(get("/api/members/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstname").value("Akram"))
                .andExpect(jsonPath("$.lastname").value("Shaik"))
                .andExpect(jsonPath("$.email").value("akram@gmail.com"));

        verify(memberService).getById(1);
    }

    //══════════════════════════════════════════════
    // TEST 3 MEMBER NOT FOUND
    //══════════════════════════════════════════════

    @Test
    @DisplayName("GET member returns 404")
    void getById_ShouldReturn404() throws Exception {

        when(memberService.getById(999))
                .thenThrow(new MemberNotFoundException(999));

        mockMvc.perform(get("/api/members/999"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Member with id 999 not found"));

        verify(memberService).getById(999);
    }

    //══════════════════════════════════════════════
    // TEST 4 GET MEMBER WITH BORROWS
    //══════════════════════════════════════════════

    @Test
    @DisplayName("GET member with borrows")
    void getWithBorrows_ShouldReturn200() throws Exception {

        when(memberService.getByIdWithBorrows(1))
                .thenReturn(memberResponseDTO);

        mockMvc.perform(get("/api/members/1/borrows"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname").value("Akram"));

        verify(memberService).getByIdWithBorrows(1);
    }
    //══════════════════════════════════════════════
    // TEST 5 SEARCH MEMBERS
    //══════════════════════════════════════════════

    @Test
    @DisplayName("GET /api/members/search returns matching members")
    void search_ShouldReturn200() throws Exception {

        when(memberService.searchByName("Akram", 0, 10))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/members/search")
                        .param("keyword", "Akram")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstname").value("Akram"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(memberService).searchByName("Akram", 0, 10);
    }

    //══════════════════════════════════════════════
    // TEST 6 GET MEMBERS BY STATUS
    //══════════════════════════════════════════════

    @Test
    @DisplayName("GET members by status")
    void getByStatus_ShouldReturn200() throws Exception {

        when(memberService.getByStatus(Member.MembershipStatus.ACTIVE))
                .thenReturn(List.of(memberResponseDTO));

        mockMvc.perform(get("/api/members/status/ACTIVE"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstname").value("Akram"))
                .andExpect(jsonPath("$[0].membershipStatus").value("ACTIVE"));

        verify(memberService).getByStatus(Member.MembershipStatus.ACTIVE);
    }

    //══════════════════════════════════════════════
    // TEST 7 SAVE MEMBER
    //══════════════════════════════════════════════

    @Test
    @DisplayName("POST /api/members creates member")
    void save_ShouldReturn200() throws Exception {

        when(memberService.save(any(MemberRequestDTO.class)))
                .thenReturn(memberResponseDTO);

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberRequestDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname").value("Akram"))
                .andExpect(jsonPath("$.lastname").value("Shaik"))
                .andExpect(jsonPath("$.email").value("akram@gmail.com"));

        verify(memberService).save(any(MemberRequestDTO.class));
    }

    //══════════════════════════════════════════════
    // TEST 8 VALIDATION FAILURE
    //══════════════════════════════════════════════

    @Test
    @DisplayName("POST invalid member returns 400")
    void save_ShouldReturn400_WhenValidationFails() throws Exception {

        MemberRequestDTO invalid = new MemberRequestDTO();
        invalid.setFirstname("");
        invalid.setLastname("");
        invalid.setEmail("abc");
        invalid.setPhone("123");

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        verify(memberService, never()).save(any(MemberRequestDTO.class));
    }
    //══════════════════════════════════════════════
    // TEST 9 UPDATE MEMBER
    //══════════════════════════════════════════════

    @Test
    @DisplayName("PUT /api/members/{id} updates member")
    void update_ShouldReturn200() throws Exception {

        when(memberService.update(eq(1), any(MemberRequestDTO.class)))
                .thenReturn(memberResponseDTO);

        mockMvc.perform(put("/api/members/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberRequestDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname").value("Akram"))
                .andExpect(jsonPath("$.lastname").value("Shaik"));

        verify(memberService).update(eq(1), any(MemberRequestDTO.class));
    }

    //══════════════════════════════════════════════
    // TEST 10 UPDATE MEMBER NOT FOUND
    //══════════════════════════════════════════════

    @Test
    @DisplayName("PUT member returns 404")
    void update_ShouldReturn404() throws Exception {

        when(memberService.update(eq(999), any(MemberRequestDTO.class)))
                .thenThrow(new MemberNotFoundException(999));

        mockMvc.perform(put("/api/members/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberRequestDTO)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Member with id 999 not found"));

        verify(memberService).update(eq(999), any(MemberRequestDTO.class));
    }

    //══════════════════════════════════════════════
    // TEST 11 SUSPEND MEMBER
    //══════════════════════════════════════════════

    @Test
    @DisplayName("PUT suspend member")
    void suspend_ShouldReturn200() throws Exception {

        MemberResponseDTO suspended = MemberResponseDTO.builder()
                .id(1)
                .firstname("Akram")
                .lastname("Shaik")
                .email("akram@gmail.com")
                .phone("9876543210")
                .memberShipDate(LocalDate.now())
                .membershipStatus(Member.MembershipStatus.SUSPENDED)
                .build();

        when(memberService.suspendMember(1))
                .thenReturn(suspended);

        mockMvc.perform(put("/api/members/1/suspend"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.membershipStatus")
                        .value("SUSPENDED"));

        verify(memberService).suspendMember(1);
    }

    //══════════════════════════════════════════════
    // TEST 12 ACTIVATE MEMBER
    //══════════════════════════════════════════════

    @Test
    @DisplayName("PUT activate member")
    void activate_ShouldReturn200() throws Exception {

        when(memberService.activateMember(1))
                .thenReturn(memberResponseDTO);

        mockMvc.perform(put("/api/members/1/activate"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.membershipStatus")
                        .value("ACTIVE"));

        verify(memberService).activateMember(1);
    }

    //══════════════════════════════════════════════
    // TEST 13 DELETE MEMBER
    //══════════════════════════════════════════════

    @Test
    @DisplayName("DELETE member")
    void delete_ShouldReturn200() throws Exception {

        doNothing().when(memberService).delete(1);

        mockMvc.perform(delete("/api/members/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Member deleted successfully"));

        verify(memberService).delete(1);
    }

    //══════════════════════════════════════════════
    // TEST 14 DELETE MEMBER NOT FOUND
    //══════════════════════════════════════════════

    @Test
    @DisplayName("DELETE member returns 404")
    void delete_ShouldReturn404() throws Exception {

        doThrow(new MemberNotFoundException(999))
                .when(memberService).delete(999);

        mockMvc.perform(delete("/api/members/999"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Member with id 999 not found"));

        verify(memberService).delete(999);
    }

}