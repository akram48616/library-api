package org.example.librarymanagementsystem.integration;

import org.example.librarymanagementsystem.DTO.RequestDTO.MemberRequestDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.MemberResponseDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.PageResponseDTO;
import org.example.librarymanagementsystem.Repository.MemberRepository;
import org.example.librarymanagementsystem.Service.MemberService;
import org.example.librarymanagementsystem.entity.Member;
import org.example.librarymanagementsystem.exception.BadRequestException;
import org.example.librarymanagementsystem.exception.DuplicateEmailException;
import org.example.librarymanagementsystem.exception.MemberNotFoundException;

import org.junit.jupiter.api.*;
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
class MemberServiceIntegrationTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    private Member member1;
    private Member member2;

    @BeforeEach
    void setUp() {

        member1 = new Member();
        member1.setFirstname("John");
        member1.setLastname("Doe");
        member1.setEmail("john@gmail.com");
        member1.setPhone("9876543210");
        member1.setMembershipDate(LocalDate.now());
        member1.setMembershipStatus(
                Member.MembershipStatus.ACTIVE
        );

        member2 = new Member();
        member2.setFirstname("James");
        member2.setLastname("Smith");
        member2.setEmail("james@gmail.com");
        member2.setPhone("9876543211");
        member2.setMembershipDate(LocalDate.now());
        member2.setMembershipStatus(
                Member.MembershipStatus.INACTIVE
        );

        memberRepository.save(member1);
        memberRepository.save(member2);
    }

    // =====================================================
    // TEST 1 - getAllMembers()
    // =====================================================

    @Test
    @DisplayName("Test 1: getAllMembers should return paginated members")
    void getAllMembers_ShouldReturnPaginatedMembers() {

        PageResponseDTO<MemberResponseDTO> result =
                memberService.getAllMembers(
                        0,
                        10,
                        "firstname",
                        "asc"
                );

        assertNotNull(result);

        assertEquals(2, result.getTotalElements());
        assertEquals(0, result.getCurrentPage());
        assertEquals(10, result.getPageSize());

        assertFalse(result.isEmpty());
        assertTrue(result.isFirst());
    }

    // =====================================================
    // TEST 2 - getById()
    // =====================================================

    @Test
    @DisplayName("Test 2: getById should return member")
    void getById_ShouldReturnMember() {

        MemberResponseDTO result =
                memberService.getById(member1.getId());

        assertNotNull(result);

        assertEquals(
                member1.getId(),
                result.getId()
        );

        assertEquals(
                "John",
                result.getFirstname()
        );

        assertEquals(
                "Doe",
                result.getLastname()
        );

        assertEquals(
                "john@gmail.com",
                result.getEmail()
        );
    }

    // =====================================================
    // TEST 3 - getById() not found
    // =====================================================

    @Test
    @DisplayName("Test 3: getById should throw MemberNotFoundException")
    void getById_ShouldThrow_WhenMemberNotFound() {

        assertThrows(
                MemberNotFoundException.class,
                () -> memberService.getById(99999)
        );
    }

    // =====================================================
    // TEST 4 - searchByName()
    // =====================================================

    @Test
    @DisplayName("Test 4: searchByName should return matching members")
    void searchByName_ShouldReturnMatchingMembers() {

        PageResponseDTO<MemberResponseDTO> result =
                memberService.searchByName(
                        "John",
                        0,
                        10
                );

        assertNotNull(result);

        assertEquals(
                1,
                result.getTotalElements()
        );

        assertEquals(
                "John",
                result.getContent()
                        .get(0)
                        .getFirstname()
        );
    }

    // =====================================================
    // TEST 5 - getByStatus()
    // =====================================================

    @Test
    @DisplayName("Test 5: getByStatus should return members by status")
    void getByStatus_ShouldReturnMembersByStatus() {

        List<MemberResponseDTO> result =
                memberService.getByStatus(
                        Member.MembershipStatus.ACTIVE
                );

        assertNotNull(result);

        assertEquals(1, result.size());

        assertEquals(
                "John",
                result.get(0).getFirstname()
        );

        assertEquals(
                "john@gmail.com",
                result.get(0).getEmail()
        );
    }

    // =====================================================
    // TEST 6 - save()
    // =====================================================

    @Test
    @DisplayName("Test 6: save should persist new member")
    void save_ShouldPersistMember() {

        MemberRequestDTO request =
                MemberRequestDTO.builder()
                        .firstname("Robert")
                        .lastname("Martin")
                        .email("robert@gmail.com")
                        .phone("9876543212")
                        .build();

        MemberResponseDTO result =
                memberService.save(request);

        assertNotNull(result);

        assertNotNull(result.getId());

        assertEquals(
                "Robert",
                result.getFirstname()
        );

        assertEquals(
                "Martin",
                result.getLastname()
        );

        assertEquals(
                "robert@gmail.com",
                result.getEmail()
        );

        assertEquals(
                "9876543212",
                result.getPhone()
        );

        // Service should automatically set these
        assertNotNull(result.getMemberShipDate());

        assertEquals(
                Member.MembershipStatus.ACTIVE,
                result.getMembershipStatus()
        );

        assertTrue(
                memberRepository.existsByEmail(
                        "robert@gmail.com"
                )
        );
    }

    // =====================================================
    // TEST 7 - save() duplicate email
    // =====================================================

    @Test
    @DisplayName("Test 7: save should throw DuplicateEmailException")
    void save_ShouldThrow_WhenEmailAlreadyExists() {

        MemberRequestDTO request =
                MemberRequestDTO.builder()
                        .firstname("Another")
                        .lastname("Person")
                        .email("john@gmail.com")
                        .phone("9876543213")
                        .build();

        DuplicateEmailException exception =
                assertThrows(
                        DuplicateEmailException.class,
                        () -> memberService.save(request)
                );

        assertNotNull(exception);
    }

    // =====================================================
    // TEST 8 - update()
    // =====================================================

    @Test
    @DisplayName("Test 8: update should modify member")
    void update_ShouldModifyMember() {

        MemberRequestDTO request =
                MemberRequestDTO.builder()
                        .firstname("Johnny")
                        .lastname("Doe")
                        .email("john@gmail.com")
                        .phone("9999999999")
                        .membershipStatus(
                                Member.MembershipStatus.ACTIVE
                        )
                        .build();

        MemberResponseDTO result =
                memberService.update(
                        member1.getId(),
                        request
                );

        assertNotNull(result);

        assertEquals(
                member1.getId(),
                result.getId()
        );

        assertEquals(
                "Johnny",
                result.getFirstname()
        );

        assertEquals(
                "Doe",
                result.getLastname()
        );

        assertEquals(
                "9999999999",
                result.getPhone()
        );

        assertEquals(
                Member.MembershipStatus.ACTIVE,
                result.getMembershipStatus()
        );
    }

    // =====================================================
    // TEST 9 - suspendMember() + activateMember()
    // =====================================================

    @Test
    @DisplayName("Test 9: suspend and activate member should change status")
    void suspendAndActivateMember_ShouldChangeStatus() {

        // Suspend
        MemberResponseDTO suspended =
                memberService.suspendMember(
                        member1.getId()
                );

        assertNotNull(suspended);

        assertEquals(
                Member.MembershipStatus.SUSPENDED,
                suspended.getMembershipStatus()
        );

        // Activate
        MemberResponseDTO activated =
                memberService.activateMember(
                        member1.getId()
                );

        assertNotNull(activated);

        assertEquals(
                Member.MembershipStatus.ACTIVE,
                activated.getMembershipStatus()
        );
    }

    // =====================================================
    // TEST 10 - delete()
    // =====================================================

    @Test
    @DisplayName("Test 10: delete should remove member")
    void delete_ShouldRemoveMember() {

        Integer id = member1.getId();

        memberService.delete(id);

        assertFalse(
                memberRepository.existsById(id)
        );

        assertEquals(
                1,
                memberRepository.count()
        );
    }
}
