package library.repository;

import jakarta.annotation.Nonnull;
import java.util.List;
import library.model.Book;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    @Nonnull
    @EntityGraph("book_get_all")
    List<Book> findAll();

    @Query("SELECT DISTINCT b FROM Book b INNER JOIN FETCH b.authors a "
            + "LEFT JOIN FETCH b.categories LEFT JOIN FETCH b.reviews WHERE a.name = :name")
    List<Book> findByAuthor(@Param("name") String author);

    @Query(value = "SELECT DISTINCT b.* FROM books b "
            + "LEFT JOIN book_authors ba ON b.id = ba.book_id "
            + "INNER JOIN book_categories bc ON b.id = bc.book_id "
            + "INNER JOIN categories c ON bc.category_id = c.id "
            + "LEFT JOIN authors a ON ba.author_id = a.id "
            + "LEFT JOIN reviews r ON b.id = r.id "
            + "WHERE c.name = :name", nativeQuery = true)
    List<Book> findByCategory(@Param("name") String category);

    @Query("SELECT DISTINCT b FROM Book b LEFT JOIN FETCH b.authors a "
            + "LEFT JOIN FETCH b.categories LEFT JOIN FETCH b.reviews WHERE b.name = :name ")
    List<Book> findByName(String name);
}
