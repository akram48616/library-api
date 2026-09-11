package org.example.librarymanagementsystem.Repository;



import org.example.librarymanagementsystem.entity.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublisherRepository extends JpaRepository<Publisher, Integer> {

    // ─── Derived Queries ───────────────────────────

    // find by name
    Publisher findByName(String name);

    // check name exists
    boolean existsByName(String name);

    // search by name containing
    List<Publisher> findByNameContaining(String keyword);

    // find by phone
    Publisher findByPhone(String phone);

    // ─── Custom @Query ─────────────────────────────

    // get publisher with all books
    @Query("SELECT DISTINCT p FROM Publisher p " +
            "LEFT JOIN FETCH p.books " +
            "WHERE p.id = :id")
    Publisher findByIdWithBooks(@Param("id") Integer id);

    // get all publishers with books
    @Query("SELECT DISTINCT p FROM Publisher p " +
            "LEFT JOIN FETCH p.books")
    List<Publisher> findAllWithBooks();
}