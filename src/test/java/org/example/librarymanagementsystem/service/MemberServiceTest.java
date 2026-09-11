package org.example.librarymanagementsystem.service;

import org.example.librarymanagementsystem.DTO.RequestDTO.MemberRequestDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.MemberResponseDTO;
import org.example.librarymanagementsystem.Repository.MemberRepository;
import org.example.librarymanagementsystem.Service.MemberService;
import org.example.librarymanagementsystem.entity.BorrowRecord;
import org.example.librarymanagementsystem.entity.Member;
import org.example.librarymanagementsystem.exception.BadRequestException;
import org.example.librarymanagementsystem.exception.DuplicateEmailException;
import org.example.librarymanagementsystem.exception.MemberNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository repository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private MemberService memberService;

    private Member member;
    private MemberResponseDTO memberResponseDTO;

    @BeforeEach
    void setUp() {
        member = new Member();
        member.setId(1);
        member.setFirstname("Shaik");
        member.setLastname("Akram");
        member.setEmail("akram@gmail.com");
        member.setMembershipStatus(Member.MembershipStatus.ACTIVE);
        member.setMembershipDate(LocalDate.of(2025, 1, 1));

        memberResponseDTO = new MemberResponseDTO();
        memberResponseDTO.setId(1);
        memberResponseDTO.setFirstname("Shaik");
        memberResponseDTO.setLastname("Akram");
        memberResponseDTO.setEmail("akram@gmail.com");
    }

    // ─── getById ─────────────────────────────────
    @Test
    void getById_whenFound_returnsDto() {
        when(repository.findById(1)).thenReturn(Optional.of(member));
        when(modelMapper.map(member, MemberResponseDTO.class)).thenReturn(memberResponseDTO);

        MemberResponseDTO result = memberService.getById(1);

        assertEquals("Shaik", result.getFirstname());
    }

    @Test
    void getById_whenNotFound_throwsMemberNotFoundException() {
        when(repository.findById(999)).thenReturn(Optional.empty());

        assertThrows(MemberNotFoundException.class, () -> memberService.getById(999));
    }

    // ─── getByIdWithBorrows ──────────────────────
    @Test
    void getByIdWithBorrows_whenNotFound_throwsMemberNotFoundException() {
        when(repository.findByIdWithBorrows(999)).thenReturn(null);

        assertThrows(MemberNotFoundException.class, () -> memberService.getByIdWithBorrows(999));
    }

    // ─── save ────────────────────────────────────
    @Test
    void save_whenEmailUnique_createsMemberWithDefaults() {
        MemberRequestDTO dto = new MemberRequestDTO();
        dto.setFirstname("Shaik");
        dto.setLastname("Akram");
        dto.setEmail("akram@gmail.com");

        Member newMember = new Member(); // no membershipDate/status set

        when(repository.existsByEmail("akram@gmail.com")).thenReturn(false);
        when(modelMapper.map(dto, Member.class)).thenReturn(newMember);
        when(repository.save(newMember)).thenReturn(newMember);
        when(modelMapper.map(newMember, MemberResponseDTO.class)).thenReturn(memberResponseDTO);

        MemberResponseDTO result = memberService.save(dto);

        assertNotNull(result);
        assertEquals(LocalDate.now(), newMember.getMembershipDate());
        assertEquals(Member.MembershipStatus.ACTIVE, newMember.getMembershipStatus());
    }

    @Test
    void save_whenEmailAlreadyExists_throwsDuplicateEmailException() {
        MemberRequestDTO dto = new MemberRequestDTO();
        dto.setEmail("akram@gmail.com");

        when(repository.existsByEmail("akram@gmail.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> memberService.save(dto));
        verify(repository, never()).save(any());
    }

    // ─── update ──────────────────────────────────
    @Test
    void update_whenEmailUnchanged_updatesWithoutDuplicateCheck() {
        MemberRequestDTO dto = new MemberRequestDTO();
        dto.setFirstname("Shaik");
        dto.setLastname("Akram Updated");
        dto.setEmail("akram@gmail.com"); // same email
        dto.setPhone("9999999999");

        when(repository.findById(1)).thenReturn(Optional.of(member));
        when(repository.save(member)).thenReturn(member);
        when(modelMapper.map(member, MemberResponseDTO.class)).thenReturn(memberResponseDTO);

        memberService.update(1, dto);

        assertEquals("Akram Updated", member.getLastname());
        verify(repository, never()).existsByEmail(anyString());
    }

    @Test
    void update_whenEmailChangedToDuplicate_throwsDuplicateEmailException() {
        MemberRequestDTO dto = new MemberRequestDTO();
        dto.setEmail("new@gmail.com"); // different email

        when(repository.findById(1)).thenReturn(Optional.of(member));
        when(repository.existsByEmail("new@gmail.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> memberService.update(1, dto));
        verify(repository, never()).save(any());
    }

    @Test
    void update_whenNotFound_throwsMemberNotFoundException() {
        MemberRequestDTO dto = new MemberRequestDTO();
        when(repository.findById(999)).thenReturn(Optional.empty());

        assertThrows(MemberNotFoundException.class, () -> memberService.update(999, dto));
        verify(repository, never()).save(any());
    }

    // ─── delete ──────────────────────────────────
    @Test
    void delete_whenNoBorrowHistory_deletesMember() {
        member.setBorrowRecords(List.of());
        when(repository.findById(1)).thenReturn(Optional.of(member));

        memberService.delete(1);

        verify(repository).deleteById(1);
    }

    @Test
    void delete_whenBorrowHistoryExists_throwsBadRequestException() {
        member.setBorrowRecords(List.of(mock(BorrowRecord.class)));
        when(repository.findById(1)).thenReturn(Optional.of(member));

        assertThrows(BadRequestException.class, () -> memberService.delete(1));
        verify(repository, never()).deleteById(anyInt());
    }

    @Test
    void delete_whenNotFound_throwsMemberNotFoundException() {
        when(repository.findById(999)).thenReturn(Optional.empty());

        assertThrows(MemberNotFoundException.class, () -> memberService.delete(999));
        verify(repository, never()).deleteById(anyInt());
    }

    // ─── suspendMember / activateMember ──────────
    @Test
    void suspendMember_setsStatusToSuspended() {
        when(repository.findById(1)).thenReturn(Optional.of(member));
        when(repository.save(member)).thenReturn(member);
        when(modelMapper.map(member, MemberResponseDTO.class)).thenReturn(memberResponseDTO);

        memberService.suspendMember(1);

        assertEquals(Member.MembershipStatus.SUSPENDED, member.getMembershipStatus());
    }

    @Test
    void activateMember_setsStatusToActive() {
        member.setMembershipStatus(Member.MembershipStatus.SUSPENDED);
        when(repository.findById(1)).thenReturn(Optional.of(member));
        when(repository.save(member)).thenReturn(member);
        when(modelMapper.map(member, MemberResponseDTO.class)).thenReturn(memberResponseDTO);

        memberService.activateMember(1);

        assertEquals(Member.MembershipStatus.ACTIVE, member.getMembershipStatus());
    }
}