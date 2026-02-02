package library.service;

import jakarta.transaction.Transactional;
import java.util.List;
import library.cache.InMemoryCache;
import library.dto.create.CategoryCreateDto;
import library.dto.get.CategoryGetDto;
import library.exception.NotFoundException;
import library.mapper.CategoryMapper;
import library.model.Category;
import library.repository.BookRepository;
import library.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private static final String CATEGORY_NOT_FOUND_MESSAGE = "Category is not found with id: ";
    private final InMemoryCache cache;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository,
                           BookRepository bookRepository,
                           InMemoryCache cache) {
        this.categoryRepository = categoryRepository;
        this.cache = cache;
    }

    public List<CategoryGetDto> getAllCategories() {
        return categoryRepository.findAll().stream().map(CategoryMapper::toDto).toList();
    }

    public CategoryGetDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CATEGORY_NOT_FOUND_MESSAGE + id));
        return CategoryMapper.toDto(category);
    }

    @Transactional
    public CategoryGetDto createCategory(CategoryCreateDto categoryDto) {
        Category categoryEntity = CategoryMapper.fromDto(categoryDto);
        cache.clear();

        return CategoryMapper.toDto(categoryRepository.save(categoryEntity));
    }

    @Transactional
    public CategoryGetDto updateCategory(Long id, CategoryCreateDto categoryDto) {
        Category categoryEntity = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CATEGORY_NOT_FOUND_MESSAGE + id));

        categoryEntity.setName(categoryDto.getName());
        cache.clear();

        return CategoryMapper.toDto(categoryRepository.save(categoryEntity));
    }

    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException(CATEGORY_NOT_FOUND_MESSAGE + id);
        }
        categoryRepository.deleteById(id);
        cache.clear();
    }
}
