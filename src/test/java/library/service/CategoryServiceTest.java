package library.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.HashSet;
import java.util.Set;
import library.cache.InMemoryCache;
import library.dto.create.CategoryCreateDto;
import library.dto.get.CategoryGetDto;
import library.exception.NotFoundException;
import library.model.Book;
import library.model.Category;
import library.repository.BookRepository;
import library.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private InMemoryCache cache;

    @InjectMocks
    private CategoryService categoryService;

    private final Book bookTest = new Book(1L, "Test Book",
            null, null, 0, null, 0, null, null);
    private final Category categoryTest = new Category(1L,
            "Test Category", Set.of(bookTest));

    @Test
    void getAllCategories_shouldReturnAllCategories() {
        Category anotherCategoryTest = new Category(2L, "Another Category", null);

        when(categoryRepository.findAll()).thenReturn(List.of(categoryTest, anotherCategoryTest));

        List<CategoryGetDto> result = categoryService.getAllCategories();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Test Category", result.get(0).getName());
        assertEquals("Another Category", result.get(1).getName());
        verify(categoryRepository).findAll();
    }

    @Test
    void getCategoryById_WhenExists_ReturnsCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoryTest));

        CategoryGetDto result = categoryService.getCategoryById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Category", result.getName());
        verify(categoryRepository).findById(1L);
    }

    @Test
    void getCategoryById_WhenNotFound_ThrowsException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> categoryService.getCategoryById(1L));
        assertEquals("Category is not found with id: " + 1L, exception.getMessage());
        verify(categoryRepository).findById(1L);
    }

    @Test
    void createCategory_WithValidInput_CreatesCategory() {
        CategoryCreateDto categoryDto = new CategoryCreateDto("New Category", List.of(1L, 2L));
        Book book1 = new Book(1L, "Test Book 1", null,
                new HashSet<>(), 0, null, 0, null, null);
        Book book2 = new Book(1L, "Test Book 2", null,
                new HashSet<>(), 0, null, 0, null, null);

        Category savedCategory = new Category(2L, categoryDto.getName(), Set.of(book1, book2));

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));
        when(bookRepository.findById(2L)).thenReturn(Optional.of(book2));
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        CategoryGetDto result = categoryService.createCategory(categoryDto);

        assertNotNull(result);
        assertEquals("New Category", result.getName());
        verify(bookRepository, times(2)).findById(anyLong());
        verify(categoryRepository).save(any(Category.class));
        verify(cache).clear();
    }

    @Test
    void createCategory_WithEmptyList_CreatesCategory() {
        CategoryCreateDto categoryDto = new CategoryCreateDto("New Category", Collections.emptyList());

        Category savedCategory = new Category(2L, categoryDto.getName(), Collections.emptySet());

        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        CategoryGetDto result = categoryService.createCategory(categoryDto);

        assertNotNull(result);
        assertEquals("New Category", result.getName());
        verify(bookRepository, never()).findById(anyLong());
        verify(categoryRepository).save(any(Category.class));
        verify(cache).clear();
    }

    @Test
    void createCategory_WithoutList_CreatesCategory() {
        CategoryCreateDto categoryDto = new CategoryCreateDto("New Category", null);

        Category savedCategory = new Category(2L, categoryDto.getName(), Collections.emptySet());

        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        CategoryGetDto result = categoryService.createCategory(categoryDto);

        assertNotNull(result);
        assertEquals("New Category", result.getName());
        verify(bookRepository, never()).findById(anyLong());
        verify(categoryRepository).save(any(Category.class));
        verify(cache).clear();
    }

    @Test
    void createCategory_WhenBookNotFound_ThrowsException() {
        CategoryCreateDto categoryDto = new CategoryCreateDto();
        categoryDto.setName("New Category");
        categoryDto.setBookIds(List.of(1L));

        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> categoryService.createCategory(categoryDto));
        assertEquals("Book is not found with id: " + 1L, exception.getMessage());
        verify(bookRepository).findById(1L);
        verify(cache, never()).clear();
    }

    @Test
    void updateCategory_WhenExists_UpdatesCategory() {
        CategoryCreateDto categoryDto = new CategoryCreateDto();
        categoryDto.setName("Updated Category");
        categoryDto.setBookIds(List.of(1L));

        Book book1 = new Book();
        book1.setId(1L);
        book1.setCategories(new HashSet<>());

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoryTest));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));
        when(categoryRepository.save(any(Category.class))).thenReturn(categoryTest);

        CategoryGetDto result = categoryService.updateCategory(1L, categoryDto);

        assertNotNull(result);
        assertEquals("Updated Category", result.getName());
        verify(categoryRepository).findById(1L);
        verify(bookRepository).findById(1L);
        verify(categoryRepository).save(any(Category.class));
        verify(cache).clear();
    }

    @Test
    void updateCategory_WhenNotFound_ShouldThrowException() {
        CategoryCreateDto categoryDto = new CategoryCreateDto();
        categoryDto.setName("Updated Category");

        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> categoryService.updateCategory(1L, categoryDto));
        assertEquals("Category is not found with id: " + 1L, exception.getMessage());
        verify(categoryRepository).findById(1L);
    }

    @Test
    void deleteCategory_WhenExists_DeletesCategory() {
        when(categoryRepository.existsById(1L)).thenReturn(true);

        categoryService.deleteCategory(1L);

        verify(categoryRepository).existsById(1L);
        verify(categoryRepository).deleteById(1L);
        verify(cache).clear();
    }

    @Test
    void deleteCategory_WhenNotFound_ThrowsException() {
        when(categoryRepository.existsById(20L)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> categoryService.deleteCategory(20L));
        assertEquals("Category is not found with id: " + 20L, exception.getMessage());
        verify(categoryRepository).existsById(20L);
        verify(categoryRepository, never()).deleteById(20L);
    }
}