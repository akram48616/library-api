package org.example.librarymanagementsystem.Repository;

import org.example.librarymanagementsystem.Repository.MemberRepository;
import org.example.librarymanagementsystem.entity.Member;
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
class MemberRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MemberRepository memberRepository;

    private Member member1;
    private Member member2;
    private Member member3;

    @BeforeEach
    void setUp() {

        member1 = new Member();
        member1.setFirstname("Shaik");
        member1.setLastname("Akram");
        member1.setEmail("akram@gmail.com");
        member1.setPhone("9502840779");
        member1.setMembershipDate(LocalDate.now());
        member1.setMembershipStatus(
                Member.MembershipStatus.ACTIVE);
        member1.setBorrowRecords(new ArrayList<>());
        entityManager.persist(member1);

        member2 = new Member();
        member2.setFirstname("Shaik");
        member2.setLastname("Sumiya");
        member2.setEmail("sumiya@gmail.com");
        member2.setPhone("9985510280");
        member2.setMembershipDate(LocalDate.now());
        member2.setMembershipStatus(
                Member.MembershipStatus.ACTIVE);
        member2.setBorrowRecords(new ArrayList<>());
        entityManager.persist(member2);

        member3 = new Member();
        member3.setFirstname("Raza");
        member3.setLastname("Khan");
        member3.setEmail("raza@gmail.com");
        member3.setPhone("8886123886");
        member3.setMembershipDate(LocalDate.now());
        member3.setMembershipStatus(
                Member.MembershipStatus.SUSPENDED);
        member3.setBorrowRecords(new ArrayList<>());
        entityManager.persist(member3);

        entityManager.flush();
    }

    // ══════════════════════════════════════════════
    // TEST 1 — findByEmail returns member
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 1: findByEmail should return member")
    void findByEmail_ShouldReturnMember() {

        Member result =
                memberRepository.findByEmail("akram@gmail.com");

        assertNotNull(result);
        assertEquals("Shaik", result.getFirstname());
        assertEquals("Akram", result.getLastname());
    }

    // ══════════════════════════════════════════════
    // TEST 2 — existsByEmail correct boolean
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 2: existsByEmail should return correct boolean")
    void existsByEmail_ShouldReturnCorrectBoolean() {

        assertTrue(memberRepository
                .existsByEmail("akram@gmail.com"));
        assertFalse(memberRepository
                .existsByEmail("none@test.com"));
    }

    // ══════════════════════════════════════════════
    // TEST 3 — findByPhone returns member
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 3: findByPhone should return member")
    void findByPhone_ShouldReturnMember() {

        Member result =
                memberRepository.findByPhone("9502840779");

        assertNotNull(result);
        assertEquals("Akram", result.getLastname());
    }

    // ══════════════════════════════════════════════
    // TEST 4 — findByMembershipStatus returns list
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 4: findByMembershipStatus should return members by status")
    void findByMembershipStatus_ShouldReturnMembers() {

        // ACTIVE — should return 2
        List<Member> active = memberRepository
                .findByMembershipStatus(
                        Member.MembershipStatus.ACTIVE);
        assertEquals(2, active.size());

        // SUSPENDED — should return 1
        List<Member> suspended = memberRepository
                .findByMembershipStatus(
                        Member.MembershipStatus.SUSPENDED);
        assertEquals(1, suspended.size());
        assertEquals("Raza",
                suspended.get(0).getFirstname());

        // INACTIVE — should return 0
        List<Member> inactive = memberRepository
                .findByMembershipStatus(
                        Member.MembershipStatus.INACTIVE);
        assertEquals(0, inactive.size());
    }

    // ══════════════════════════════════════════════
    // TEST 5 — search by name paged
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 5: search by name should return matching members")
    void searchByName_ShouldReturnMatchingMembers() {

        Pageable pageable = PageRequest.of(0, 10);

        // search "Shaik" — finds 2 members
        Page<Member> shaikResult = memberRepository
                .findByFirstnameContainingOrLastnameContaining(
                        "Shaik", "Shaik", pageable);
        assertEquals(2, shaikResult.getTotalElements());

        // search "Akram" — finds 1 member
        Page<Member> akramResult = memberRepository
                .findByFirstnameContainingOrLastnameContaining(
                        "Akram", "Akram", pageable);
        assertEquals(1, akramResult.getTotalElements());

        // search "xyz" — finds nothing
        Page<Member> emptyResult = memberRepository
                .findByFirstnameContainingOrLastnameContaining(
                        "xyz", "xyz", pageable);
        assertEquals(0, emptyResult.getTotalElements());
    }

    // ══════════════════════════════════════════════
    // TEST 6 — findAll paged returns correct page
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 6: findAll paged should return correct page")
    void findAll_Paged_ShouldReturnCorrectPage() {

        Pageable pageable = PageRequest.of(0, 2,
                Sort.by("firstname").ascending());
        Page<Member> page = memberRepository.findAll(pageable);

        assertEquals(3, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
        assertEquals(2, page.getContent().size());
        assertTrue(page.isFirst());
        assertFalse(page.isLast());
    }

    // ══════════════════════════════════════════════
    // TEST 7 — findByIdWithBorrows returns member
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 7: findByIdWithBorrows should return member with borrows")
    void findByIdWithBorrows_ShouldReturnMember() {

        Member result = memberRepository
                .findByIdWithBorrows(member1.getId());

        assertNotNull(result);
        assertEquals("Akram", result.getLastname());
        assertNotNull(result.getBorrowRecords());
    }

    // ══════════════════════════════════════════════
    // TEST 8 — countActiveMembers returns correct count
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 8: countActiveMembers should return correct count")
    void countActiveMembers_ShouldReturnCorrectCount() {

        Long count = memberRepository.countActiveMembers();
        assertEquals(2L, count);
    }

    // ══════════════════════════════════════════════
    // TEST 9 — save persists member
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 9: save should persist member")
    void save_ShouldPersistMember() {

        Member newMember = new Member();
        newMember.setFirstname("New");
        newMember.setLastname("Member");
        newMember.setEmail("new@gmail.com");
        newMember.setPhone("1234567890");
        newMember.setMembershipDate(LocalDate.now());
        newMember.setMembershipStatus(
                Member.MembershipStatus.ACTIVE);
        newMember.setBorrowRecords(new ArrayList<>());

        Member saved = memberRepository.save(newMember);

        assertNotNull(saved.getId());
        assertEquals("New", saved.getFirstname());
        assertEquals(4, memberRepository.count());
    }

    // ══════════════════════════════════════════════
    // TEST 10 — deleteById removes member
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 10: deleteById should remove member")
    void deleteById_ShouldRemoveMember() {

        Integer id = member1.getId();
        memberRepository.deleteById(id);
        entityManager.flush();

        assertFalse(memberRepository.existsById(id));
        assertEquals(2, memberRepository.count());
    }
}