package library.mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import library.dto.create.BookCreateDto;
import library.dto.get.BookGetDto;
import library.model.Author;
import library.model.Book;
import library.model.Category;
import library.model.Review;
import lombok.experimental.UtilityClass;

@UtilityClass
public class BookMapper {
    public BookGetDto toDto(Book book) {
        BookGetDto dto = new BookGetDto();

        dto.setId(book.getId());
        dto.setName(book.getName());
        dto.setPageAmount(book.getPageAmount());
        dto.setYear(book.getYear());
        dto.setRating(book.getRating());

        Set<Author> authors = book.getAuthors();
        Set<Category> categories = book.getCategories();
        List<Review> reviews = book.getReviews();
        if (authors != null) {
            dto.setAuthors(book.getAuthors().stream()
                    .map(AuthorMapper::toDto)
                    .collect(Collectors.toSet()));
        }
        if (categories != null) {
            dto.setCategories(book.getCategories().stream()
                    .map(CategoryMapper::toDto)
                    .collect(Collectors.toSet()));
        }
        if (reviews != null) {
            dto.setReviews(book.getReviews().stream()
                    .map(ReviewMapper::toDto)
                    .toList());
        }
        return dto;

    }

    public Book fromDto(BookCreateDto dto) {
        Book entity = new Book();

        entity.setName(dto.getName());
        entity.setPageAmount(dto.getPageAmount());
        entity.setYear(dto.getYear());

        return entity;
    }
}
