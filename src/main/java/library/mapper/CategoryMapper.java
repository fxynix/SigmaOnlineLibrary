package library.mapper;

import library.dto.create.CategoryCreateDto;
import library.dto.get.CategoryGetDto;
import library.model.Category;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CategoryMapper {
    public CategoryGetDto toDto(Category entity) {
        CategoryGetDto dto = new CategoryGetDto();

        dto.setId(entity.getId());
        dto.setName(entity.getName());

        if (entity.getBooks() != null) {
            dto.setCount(entity.getBooks().size());
        }

        return dto;
    }

    public Category fromDto(CategoryCreateDto dto) {
        Category entity = new Category();

        entity.setName(dto.getName());

        return entity;
    }
}