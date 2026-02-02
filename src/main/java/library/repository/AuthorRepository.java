package library.repository;

import jakarta.annotation.Nonnull;
import java.util.List;
import library.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
    @Nonnull
    @Query("SELECT a FROM Author a LEFT JOIN a.books")
    List<Author> findAll();
}