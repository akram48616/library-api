package org.example.librarymanagementsystem.Repository;

import org.example.librarymanagementsystem.entity.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorRepository extends JpaRepository<Author,Integer> {
    Author findByEmail(String email);


    boolean existsByEmail(String email);
    List<Author> findByNationality(String nationality);


    Page<Author> findAll(Pageable pageable);
    Page<Author> findByFirstnameContainsOrLastnameContains(String firstname, String lastname, Pageable pageable);



    @Query("SELECT DISTINCT  a FROM Author a "+
    "LEFT JOIN FETCH a.books "+
    "WHERE a.id=:id")
    Author findByIdWithBooks(@Param("id")Integer id);
    // get all authors with books
    @Query("SELECT DISTINCT a FROM Author a " +
            "LEFT JOIN FETCH a.books")
    List<Author> findAllWithBooks();



}
