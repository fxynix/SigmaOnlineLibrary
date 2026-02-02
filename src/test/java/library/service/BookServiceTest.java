package library.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import library.cache.InMemoryCache;
import library.dto.create.BookCreateDto;
import library.dto.get.BookGetDto;
import library.exception.NotFoundException;
import library.mapper.BookMapper;
import library.model.Author;
import library.model.Book;
import library.model.Category;
import library.repository.AuthorRepository;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {
    @Mock
    private BookRepository bookRepository;
    @Mock
    private AuthorRepository authorRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private InMemoryCache cache;

    @InjectMocks
    private BookService bookService;

    private final Author authorTest = new Author(1L, "Test Author",
            "Info", null);
    private final Category categoryTest = new Category(1L,
            "Test Category", null);
    private final Book bookTest = new Book(1L, "Test Book",
            Set.of(authorTest), Set.of(categoryTest), 100, null, 1000, null, null);

    @Test
    void getAllBooks_ReturnsAllBooks() {
        Book anotherBookTest = new Book(2L, "Another Test Book",
                Set.of(authorTest), Set.of(categoryTest), 100,
                null, 1000, null, null);

        when(bookRepository.findAll()).thenReturn(List.of(bookTest, anotherBookTest));

        List<BookGetDto> result = bookService.getAllBooks();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Test Book", result.get(0).getName());
        assertEquals("Another Test Book", result.get(1).getName());
        verify(bookRepository).findAll();
    }

    @Test
    void getBookById_WhenNotCached_ReturnsBook() {
        String key = "book_by_id_1";

        when(cache.containsKey(key)).thenReturn(false);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(bookTest));

        BookGetDto result = bookService.getBookById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Book", result.getName());
        verify(bookRepository).findById(1L);
        verify(cache).put(key, result);
    }

    @Test
    void getBookById_WhenCached_ReturnsBook() {
        String key = "book_by_id_1";

        when(cache.containsKey(key)).thenReturn(true);
        when(cache.get(key)).thenReturn(BookMapper.toDto(bookTest));

        BookGetDto result = bookService.getBookById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Book", result.getName());
        verify(bookRepository, never()).findById(anyLong());
        verify(cache).get(key);
    }

    @Test
    void getBookById_WhenNotFound_ThrowsException() {
        String key = "book_by_id_20";

        when(bookRepository.findById(20L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookService.getBookById(20L));

        assertEquals("Book is not found with id: " + 20L, exception.getMessage());
        verify(bookRepository).findById(20L);
        verify(cache, never()).get(key);
    }

    @Test
    void getBookByName_WhenNotCached_ReturnsBooks() {
        String key = "books_by_name_Test Book";

        when(cache.containsKey(key)).thenReturn(false);
        when(bookRepository.findByName("Test Book")).thenReturn(List.of(bookTest));

        List<BookGetDto> result = bookService.getBookByName("Test Book");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Book", result.get(0).getName());
        verify(bookRepository).findByName("Test Book");
        verify(cache).put(key, result);
    }

    @Test
    void getBookByName_WhenCached_ReturnsBooks() {
        String key = "books_by_name_Test Book";

        when(cache.containsKey(key)).thenReturn(true);
        when(cache.get(key)).thenReturn(List.of(BookMapper.toDto(bookTest)));

        List<BookGetDto> result = bookService.getBookByName("Test Book");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Book", result.get(0).getName());
        verify(bookRepository, never()).findByName("Test Book");
        verify(cache).get(key);
    }

    @Test
    void getBookByName_WhenNotFound_ThrowsException() {
        String key = "books_by_name_Nonexistent Book";

        when(bookRepository.findByName("Nonexistent Book")).thenReturn(Collections.emptyList());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookService.getBookByName("Nonexistent Book"));

        assertEquals("Book not found with name:" + "Nonexistent Book", exception.getMessage());
        verify(bookRepository).findByName("Nonexistent Book");
        verify(cache, never()).get(key);
    }

    @Test
    void getBookByAuthor_WhenNotCached_ReturnsBooks() {
        String key = "books_by_author_Test Author";

        when(bookRepository.findByAuthor("Test Author")).thenReturn(List.of(bookTest));
        when(cache.containsKey(key)).thenReturn(false);

        List<BookGetDto> result = bookService.getBookByAuthor("Test Author");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Author", result.get(0).getAuthors().iterator().next().getName());
        verify(bookRepository).findByAuthor("Test Author");
        verify(cache).put(key, result);
    }

    @Test
    void getBookByAuthor_WhenCached_ReturnsBooks() {
        String key = "books_by_author_Test Author";

        when(cache.containsKey(key)).thenReturn(true);
        when(cache.get(key)).thenReturn(List.of(BookMapper.toDto(bookTest)));

        List<BookGetDto> result = bookService.getBookByAuthor("Test Author");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Author", result.get(0).getAuthors().iterator().next().getName());
        verify(bookRepository, never()).findByAuthor("Test Author");
        verify(cache).get(key);
    }

    @Test
    void getBookByAuthor_WhenNotFound_ThrowsException() {
        String key = "books_by_author_Nonexistent Author";

        when(bookRepository.findByAuthor("Nonexistent Author")).thenReturn(Collections.emptyList());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookService.getBookByAuthor("Nonexistent Author"));

        assertEquals("Book not found with author's name: " + "Nonexistent Author", exception.getMessage());
        verify(bookRepository).findByAuthor("Nonexistent Author");
        verify(cache, never()).get(key);
    }

    @Test
    void getBookByCategory_WhenNotCached_ReturnsBooks() {
        String key = "books_by_category_Test Category";

        when(cache.containsKey(key)).thenReturn(false);
        when(bookRepository.findByCategory("Test Category")).thenReturn(List.of(bookTest));

        List<BookGetDto> result = bookService.getBookByCategory("Test Category");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Category", result.get(0).getCategories().iterator().next().getName());
        verify(bookRepository).findByCategory("Test Category");
        verify(cache).put(key, result);
    }

    @Test
    void getBookByCategory_WhenCached_ReturnsBooks() {
        String key = "books_by_category_Test Category";

        when(cache.containsKey(key)).thenReturn(true);
        when(cache.get(key)).thenReturn(List.of(BookMapper.toDto(bookTest)));

        List<BookGetDto> result = bookService.getBookByCategory("Test Category");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Category", result.get(0).getCategories().iterator().next().getName());
        verify(bookRepository, never()).findByCategory("Test Category");
        verify(cache).get(key);
    }

    @Test
    void getBookByCategory_WhenNotFound_ThrowsException() {
        String key = "books_by_category_Nonexistent Category";

        when(bookRepository.findByCategory("Nonexistent Category")).thenReturn(Collections.emptyList());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookService.getBookByCategory("Nonexistent Category"));
        assertEquals("Book not found with category's name: "
                + "Nonexistent Category", exception.getMessage());
        verify(bookRepository).findByCategory("Nonexistent Category");
        verify(cache, never()).get(key);
    }

    @Test
    void createBook_WithValidData_CreatesBook() {
        BookCreateDto bookDto = new BookCreateDto("New Book", Set.of(1L),
                Set.of(1L), 20, 2021);
        Book savedBook = new Book(2L, "New Book", Set.of(authorTest),
                Set.of(categoryTest), 20, null, 2021, null, null);

        when(authorRepository.findById(1L)).thenReturn(Optional.of(authorTest));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoryTest));
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        BookGetDto result = bookService.createBook(bookDto);

        assertNotNull(result);
        assertEquals("New Book", result.getName());
        assertEquals(20, result.getPageAmount());
        assertEquals(2021, result.getYear());
        assertEquals(1, result.getCategories().size());
        assertEquals(1, result.getAuthors().size());
        verify(authorRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(bookRepository).save(any(Book.class));
        verify(cache).clear();
    }

    @Test
    void createBook_WithEmptySets_CreatesBook() {
        BookCreateDto bookDto = new BookCreateDto("New Book",
                Collections.emptySet(), Collections.emptySet(), 20, 2021);
        Book savedBook = new Book(2L, "New Book",
                Collections.emptySet(), Collections.emptySet(), 20,
                null, 2021, null, null);

        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        BookGetDto result = bookService.createBook(bookDto);

        assertNotNull(result);
        assertEquals("New Book", result.getName());
        assertEquals(20, result.getPageAmount());
        assertEquals(2021, result.getYear());
        assertTrue(result.getCategories().isEmpty());
        assertTrue(result.getAuthors().isEmpty());
        verify(authorRepository, never()).findById(anyLong());
        verify(categoryRepository, never()).findById(anyLong());
        verify(bookRepository).save(any(Book.class));
        verify(cache).clear();
    }

    @Test
    void createBook_WithoutSets_CreatesBook() {
        BookCreateDto bookDto = new BookCreateDto("New Book",
                null, null, 20, 2021);
        Book savedBook = new Book(2L, "New Book", Collections.emptySet(),
                Collections.emptySet(), 20, null, 2021, null, null);

        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        BookGetDto result = bookService.createBook(bookDto);

        assertNotNull(result);
        assertEquals("New Book", result.getName());
        assertEquals(20, result.getPageAmount());
        assertEquals(2021, result.getYear());
        assertTrue(result.getCategories().isEmpty());
        assertTrue(result.getAuthors().isEmpty());
        verify(authorRepository, never()).findById(anyLong());
        verify(categoryRepository, never()).findById(anyLong());
        verify(bookRepository).save(any(Book.class));
        verify(cache).clear();
    }

    @Test
    void createBook_WhenAuthorNotFound_ThrowsException() {
        BookCreateDto bookDto = new BookCreateDto("New Book", Set.of(20L),
                Set.of(1L), 20, 2021);

        when(authorRepository.findById(20L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookService.createBook(bookDto));
        assertEquals("Author is not found with id: " + 20L, exception.getMessage());
        verify(authorRepository).findById(20L);
        verify(cache, never()).clear();
    }

    @Test
    void createBook_WhenCategoryNotFound_ThrowsException() {
        BookCreateDto bookDto = new BookCreateDto("New Book", Set.of(1L),
                Set.of(20L), 20, 2021);

        when(authorRepository.findById(1L)).thenReturn(Optional.of(authorTest));
        when(categoryRepository.findById(20L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookService.createBook(bookDto));
        assertEquals("Category is not found with id: " + 20L, exception.getMessage());
        verify(authorRepository).findById(1L);
        verify(categoryRepository).findById(20L);
        verify(cache, never()).clear();
    }

    @Test
    void updateBook_WithValidData_UpdatesBook() {
        BookCreateDto bookDto = new BookCreateDto("Updated Book", Set.of(1L),
                Set.of(1L), 20, 2021);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(bookTest));
        when(authorRepository.findById(1L)).thenReturn(Optional.of(authorTest));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoryTest));
        when(bookRepository.save(any(Book.class))).thenReturn(bookTest);

        BookGetDto result = bookService.updateBook(1L, bookDto);

        assertNotNull(result);
        assertEquals("Updated Book", result.getName());
        assertEquals(20, result.getPageAmount());
        verify(bookRepository).findById(1L);
        verify(authorRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(bookRepository).save(any(Book.class));
        verify(cache).clear();
    }

    @Test
    void updateBook_WhenBookNotFound_ThrowsException() {
        BookCreateDto bookDto = new BookCreateDto("Updated Book",
                Set.of(1L), Set.of(1L), 20, 2021);

        when(bookRepository.findById(20L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookService.updateBook(20L, bookDto));
        assertEquals("Book is not found with id: " + 20L, exception.getMessage());
        verify(bookRepository).findById(20L);
    }

    @Test
    void updateBook_WhenAuthorNotFound_ThrowsException() {
        BookCreateDto bookDto = new BookCreateDto("Updated Book", Set.of(20L),
                Set.of(1L), 20, 2021);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(bookTest));
        when(authorRepository.findById(20L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookService.updateBook(1L, bookDto));
        assertEquals("Author is not found with id: " + 20L, exception.getMessage());
        verify(bookRepository).findById(1L);
        verify(authorRepository).findById(20L);
    }

    @Test
    void updateBook_WhenCategoryNotFound_ThrowsException() {
        BookCreateDto bookDto = new BookCreateDto("Updated Book", Set.of(1L),
                Set.of(20L), 20, 2021);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(bookTest));
        when(authorRepository.findById(1L)).thenReturn(Optional.of(authorTest));
        when(categoryRepository.findById(20L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookService.updateBook(1L, bookDto));
        assertEquals("Category is not found with id: " + 20L, exception.getMessage());
        verify(bookRepository).findById(1L);
        verify(authorRepository).findById(1L);
        verify(categoryRepository).findById(20L);
    }

    @Test
    void deleteBook_WhenExists_DeletesBook() {
        when(bookRepository.existsById(1L)).thenReturn(true);

        bookService.deleteBook(1L);

        verify(bookRepository).existsById(1L);
        verify(bookRepository).deleteById(1L);
        verify(cache).clear();
    }

    @Test
    void deleteBook_WhenNotFound_ThrowsException() {
        when(bookRepository.existsById(20L)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookService.deleteBook(20L));
        assertEquals("Book is not found with id: " + 20L, exception.getMessage());
        verify(bookRepository).existsById(20L);
        verify(bookRepository, never()).deleteById(20L);
    }
}