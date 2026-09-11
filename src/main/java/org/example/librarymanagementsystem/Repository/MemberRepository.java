package org.example.librarymanagementsystem.Repository;

import org.example.librarymanagementsystem.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, Integer> {

    // ─── Derived Queries ───────────────────────────

    // find by email
    Member findByEmail(String email);

    // check email exists
    boolean existsByEmail(String email);

    // find by phone
    Member findByPhone(String phone);

    // find by membership status
    List<Member> findByMembershipStatus(
            Member.MembershipStatus status);

    // search by firstname or lastname
    List<Member> findByFirstnameContainingOrLastnameContaining(
            String firstname, String lastname);

    // ─── Pagination ────────────────────────────────

    // get all members paged
    Page<Member> findAll(Pageable pageable);

    // get by status paged
    Page<Member> findByMembershipStatus(
            Member.MembershipStatus status, Pageable pageable);

    // search by name paged
    Page<Member> findByFirstnameContainingOrLastnameContaining(
            String firstname, String lastname, Pageable pageable);

    // ─── Custom @Query ─────────────────────────────

    // get member with all borrow records
    @Query("SELECT DISTINCT m FROM Member m " +
            "LEFT JOIN FETCH m.borrowRecords " +
            "WHERE m.id = :id")
    Member findByIdWithBorrows(@Param("id") Integer id);

    // count active members
    @Query("SELECT COUNT(m) FROM Member m "+
    "WHERE m.membershipStatus='ACTIVE'")
    Long countActiveMembers();
    long countByMembershipStatus(Member.MembershipStatus status);


}