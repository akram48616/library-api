package org.example.librarymanagementsystem.Repository;
import org.example.librarymanagementsystem.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {

    // ─── Derived Queries ───────────────────────────

    // find by isbn
    Optional<Book> findByIsbn(String isbn);

    // check isbn exists
    boolean existsByIsbn(String isbn);

    // find by title containing
    List<Book> findByTitleContaining(String keyword);

    // find available books (stock > 0)
    List<Book> findByStockGreaterThan(int stock);

    // find by publisher
    List<Book> findByPublisherId(Integer publisherId);

    // find by price range
    List<Book> findByPriceBetween(double min, double max);

    // ─── Pagination ────────────────────────────────

    // get all books paged
    Page<Book> findAll(Pageable pageable);

    // search by title paged
    Page<Book> findByTitleContaining(String keyword, Pageable pageable);

    // get available books paged
    Page<Book> findByStockGreaterThan(int stock, Pageable pageable);

    // get by publisher paged
    Page<Book> findByPublisherId(Integer publisherId, Pageable pageable);

    // ─── Custom @Query ─────────────────────────────

    // get book with authors + categories
    @Query("SELECT DISTINCT b FROM Book b " +
            "LEFT JOIN FETCH b.authors " +
            "LEFT JOIN FETCH b.categories " +
            "LEFT JOIN FETCH b.publisher " +
            "WHERE b.id = :id")
    Optional<Book> findByIdWithDetails(@Param("id") Integer id);

    // get all books with details paged
    @Query(value = "SELECT DISTINCT b FROM Book b " +
            "LEFT JOIN FETCH b.authors " +
            "LEFT JOIN FETCH b.categories " +
            "LEFT JOIN FETCH b.publisher",
            countQuery = "SELECT COUNT(DISTINCT b) FROM Book b")
    Page<Book> findAllWithDetails(Pageable pageable);

    // get books by category
    @Query("SELECT DISTINCT b FROM Book b " +
            "JOIN b.categories c " +
            "WHERE c.id = :categoryId")
    List<Book> findByCategoryId(@Param("categoryId") Integer categoryId);

    // get books by author
    @Query("SELECT DISTINCT b FROM Book b " +
            "JOIN b.authors a " +
            "WHERE a.id = :authorId")
    List<Book> findByAuthorId(@Param("authorId") Integer authorId);

    // update stock
    @Modifying
    @Transactional
    @Query("UPDATE Book b SET b.stock = b.stock - 1 WHERE b.id = :id AND b.stock > 0")
    int decreaseStock(@Param("id") Integer id);

    @Modifying
    @Transactional
    @Query("UPDATE Book b SET b.stock = b.stock + 1 WHERE b.id = :id")
    int increaseStock(@Param("id") Integer id);

    // count available books
    @Query("SELECT COUNT(b) FROM Book b WHERE b.stock > 0")
    Long countAvailableBooks();
    long countByStockGreaterThan(int stock);
}