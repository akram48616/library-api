package org.example.librarymanagementsystem.Repository;




import org.example.librarymanagementsystem.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    // ─── Derived Queries ───────────────────────────

    // find by name
    Category findByName(String name);

    // check name exists
    boolean existsByName(String name);

    // search by name containing
    List<Category> findByNameContaining(String keyword);

    // ─── Custom @Query ─────────────────────────────

    // get category with all books
    @Query("SELECT DISTINCT c FROM Category c " +
            "LEFT JOIN FETCH c.books " +
            "WHERE c.id = :id")
    Category findByIdWithBooks(@Param("id") Integer id);

    // get all categories with books
    @Query("SELECT DISTINCT c FROM Category c " +
            "LEFT JOIN FETCH c.books")
    List<Category> findAllWithBooks();
}