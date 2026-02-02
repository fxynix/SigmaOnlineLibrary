package library.repository;

import java.util.List;
import java.util.Optional;
import library.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("SELECT r FROM Review r INNER JOIN r.book b WHERE b.id = :bookId")
    List<Review> findByBookId(@Param("bookId") Long bookId);

    Optional<Review> findByIdAndBookId(Long id, Long bookId);
}