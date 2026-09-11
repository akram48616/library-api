package org.example.librarymanagementsystem.Service;

import lombok.extern.slf4j.Slf4j;
import org.example.librarymanagementsystem.DTO.RequestDTO.MemberRequestDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.MemberResponseDTO;

import org.example.librarymanagementsystem.DTO.ResponseDTO.PageResponseDTO;
import org.example.librarymanagementsystem.Repository.MemberRepository;
import org.example.librarymanagementsystem.entity.Member;
import org.example.librarymanagementsystem.exception.BadRequestException;
import org.example.librarymanagementsystem.exception.DuplicateEmailException;
import org.example.librarymanagementsystem.exception.MemberNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MemberService {

    @Autowired
    private MemberRepository repository;

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private EmailService emailService;

    private MemberResponseDTO toDTO(Member member) {
        return modelMapper.map(member, MemberResponseDTO.class);
    }

    private PageResponseDTO<MemberResponseDTO> toPageResponse(
            Page<Member> page) {

        List<MemberResponseDTO> content = page.getContent()
                .stream().map(this::toDTO).collect(Collectors.toList());

        return PageResponseDTO.<MemberResponseDTO>builder()
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
            value = "members",
            key = "'all:' + #page + ':' + #size + ':'" +
                    "+ #sortBy + ':' + #direction"
    )
    @Transactional(readOnly = true)
    public PageResponseDTO<MemberResponseDTO> getAllMembers(
            int page, int size,
            String sortBy, String direction) {
        log.info("Fetching all members - page={}, size={}, sortBy={}, direction={}", page, size, sortBy, direction);

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return toPageResponse(repository.findAll(pageable));
    }

    // ─── GET by id ─────────────────────────────────
    @Cacheable(value = "member", key = "#id")
    @Transactional(readOnly = true)
    public MemberResponseDTO getById(Integer id) {
        log.info("Fetching member by id={}", id);
        Member member = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Member not found for id={}", id);
                    return new MemberNotFoundException(id);
                });
        return toDTO(member);
    }

    // ─── GET by id with borrows ────────────────────
    @Cacheable(value = "member", key = "'with_borrows:' + #id")
    @Transactional(readOnly = true)
    public MemberResponseDTO getByIdWithBorrows(Integer id) {
        log.info("Fetching member by id={} with borrows", id);
        Member member = repository.findByIdWithBorrows(id);
        if (member == null) {
            log.warn("Member not found for id={}", id);
            throw new MemberNotFoundException(id);
        }
        return toDTO(member);
    }

    // ─── SEARCH by name ────────────────────────────
    @Cacheable(
            value = "members",
            key = "'search:' + #keyword + ':' + #page + ':' + #size"
    )
    @Transactional(readOnly = true)
    public PageResponseDTO<MemberResponseDTO> searchByName(
            String keyword, int page, int size) {
        log.info("Searching members by name keyword='{}', page={}, size={}", keyword, page, size);

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("firstname").ascending());

        return toPageResponse(
                repository.findByFirstnameContainingOrLastnameContaining(
                        keyword, keyword, pageable));
    }

    // ─── GET by status ─────────────────────────────
    @Cacheable(
            value = "members",
            key = "'status:' + #status"
    )
    @Transactional(readOnly = true)
    public List<MemberResponseDTO> getByStatus(
            Member.MembershipStatus status) {
        log.info("Fetching members by status={}", status);
        List<MemberResponseDTO> members = repository.findByMembershipStatus(status)
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
        log.debug("Found {} members with status={}", members.size(), status);
        return members;
    }

    // ─── POST save ─────────────────────────────────
    @CacheEvict(value = "members", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public MemberResponseDTO save(MemberRequestDTO dto) {
        log.info("Saving new member email='{}'", dto.getEmail());

        if (repository.existsByEmail(dto.getEmail())) {
            log.warn("Duplicate email on save attempt: '{}'", dto.getEmail());
            throw new DuplicateEmailException(dto.getEmail());
        }

        Member member = modelMapper.map(dto, Member.class);

        if (member.getMembershipDate() == null)
            member.setMembershipDate(LocalDate.now());

        if (member.getMembershipStatus() == null)
            member.setMembershipStatus(
                    Member.MembershipStatus.ACTIVE);

        Member saved = repository.save(member);
        log.info("Save method running on thread: {}", Thread.currentThread().getName());
        log.info("Member saved successfully with id={}", saved.getId());
        emailService.sendWelcomeEmail(saved.getEmail(), saved.getFirstname());
        return toDTO(saved);
    }

    // ─── PUT update ────────────────────────────────
    @CacheEvict(value = "members", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public MemberResponseDTO update(Integer id,
                                    MemberRequestDTO dto) {
        log.info("Updating member id={}", id);

        Member existing = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Member not found for update, id={}", id);
                    return new MemberNotFoundException(id);
                });

        if (!existing.getEmail().equals(dto.getEmail())
                && repository.existsByEmail(dto.getEmail())) {
            log.warn("Duplicate email on update attempt: '{}'", dto.getEmail());
            throw new DuplicateEmailException(dto.getEmail());
        }

        existing.setFirstname(dto.getFirstname());
        existing.setLastname(dto.getLastname());
        existing.setEmail(dto.getEmail());
        existing.setPhone(dto.getPhone());

        if (dto.getMembershipStatus() != null)
            existing.setMembershipStatus(dto.getMembershipStatus());

        Member updated = repository.save(existing);
        log.info("Member updated successfully, id={}", updated.getId());
        return toDTO(updated);
    }

    // ─── DELETE ────────────────────────────────────
    @CacheEvict(value = "members", allEntries = true)
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer id) {
        log.info("Deleting member id={}", id);

        Member member = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Member not found for delete, id={}", id);
                    return new MemberNotFoundException(id);
                });

        if (member.getBorrowRecords() != null
                && !member.getBorrowRecords().isEmpty()) {
            log.warn("Delete blocked for member id={} — has borrow history", id);
            throw new BadRequestException(
                    "Cannot delete member with id " + id +
                            " — has borrow history.");
        }

        repository.deleteById(id);
        log.info("Member deleted successfully, id={}", id);
    }

    // ─── SUSPEND member ────────────────────────────
    @CacheEvict(value = "members", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public MemberResponseDTO suspendMember(Integer id) {
        log.info("Suspending member id={}", id);
        Member member = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Member not found for suspend, id={}", id);
                    return new MemberNotFoundException(id);
                });
        member.setMembershipStatus(
                Member.MembershipStatus.SUSPENDED);
        Member updated = repository.save(member);
        log.info("Member id={} suspended", updated.getId());
        return toDTO(updated);
    }

    // ─── ACTIVATE member ───────────────────────────
    @CacheEvict(value = "members", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public MemberResponseDTO activateMember(Integer id) {
        log.info("Activating member id={}", id);
        Member member = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Member not found for activate, id={}", id);
                    return new MemberNotFoundException(id);
                });
        member.setMembershipStatus(
                Member.MembershipStatus.ACTIVE);
        Member updated = repository.save(member);
        log.info("Member id={} activated", updated.getId());
        return toDTO(updated);
    }
}