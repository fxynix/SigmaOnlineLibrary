package library.repository;

import jakarta.annotation.Nonnull;
import java.util.List;
import library.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Nonnull
    @Query("SELECT с FROM Category с LEFT JOIN с.books")
    List<Category> findAll();
}
