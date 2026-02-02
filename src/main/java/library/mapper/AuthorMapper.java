package library.mapper;

import java.util.HashSet;
import library.dto.create.AuthorCreateDto;
import library.dto.get.AuthorGetDto;
import library.model.Author;
import library.model.Book;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AuthorMapper {
    public AuthorGetDto toDto(Author entity) {
        AuthorGetDto dto = new AuthorGetDto();

        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setInfo(entity.getInfo());

        if (entity.getBooks() != null) {
            dto.setBooks(entity.getBooks().stream()
                    .map(Book::getName)
                    .toList());
        }

        return dto;
    }

    public Author fromDto(AuthorCreateDto dto) {
        Author entity = new Author();

        entity.setName(dto.getName());
        entity.setInfo(dto.getInfo());
        entity.setBooks(new HashSet<>());

        return entity;
    }
}
