package library.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import library.cache.InMemoryCache;
import library.dto.create.AuthorCreateDto;
import library.dto.get.AuthorGetDto;
import library.exception.NotFoundException;
import library.model.Author;
import library.model.Book;
import library.repository.AuthorRepository;
import library.repository.BookRepository;
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
class AuthorServiceTest {
    @Mock
    private AuthorRepository authorRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private InMemoryCache cache;

    @InjectMocks
    private AuthorService authorService;

    private final Book bookTest = new Book(1L, "Test Book",
            null, null, 0, null, 0, null, null);
    private final Author authorTest = new Author(1L, "Test Author",
            "Info", Set.of(bookTest));

    @Test
    void getAllAuthors_ReturnsAllAuthors() {
        Author anotherAuthorTest = new Author(2L, "Another Test Author",
                "Info", Set.of(bookTest));

        when(authorRepository.findAll()).thenReturn(List.of(authorTest, anotherAuthorTest));

        List<AuthorGetDto> result = authorService.getAllAuthors();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Test Author", result.get(0).getName());
        assertEquals("Another Test Author", result.get(1).getName());
        verify(authorRepository).findAll();
    }

    @Test
    void getAuthorById_WhenExists_ReturnsAuthor() {
        when(authorRepository.findById(1L)).thenReturn(Optional.of(authorTest));

        AuthorGetDto result = authorService.getAuthorById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Author", result.getName());
        verify(authorRepository).findById(1L);
    }

    @Test
    void getAuthorById_WhenNotFound_ThrowsException() {
        when(authorRepository.findById(5L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> authorService.getAuthorById(5L));

        assertEquals("Author is not found with id: " + 5L, exception.getMessage());
        verify(authorRepository).findById(5L);
    }

    @Test
    void createAuthor_WithValidData_CreatesAuthor() {
        Book book1 = new Book(1L, "Test Book 1", new HashSet<>(),
                null, 0, null, 0, null, null);
        Book book2 = new Book(2L, "Test Book 2", new HashSet<>(),
                null, 0, null, 0, null, null);

        AuthorCreateDto authorDto = new AuthorCreateDto("New Test Author",
                "Info", List.of(book1.getId(), book2.getId()));
        Author savedAuthor = new Author(2L, authorDto.getName(), authorDto.getInfo(), Set.of(book1, book2));

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));
        when(bookRepository.findById(2L)).thenReturn(Optional.of(book2));
        when(authorRepository.save(any(Author.class))).thenReturn(savedAuthor);

        AuthorGetDto result = authorService.createAuthor(authorDto);

        assertNotNull(result);
        assertEquals("New Test Author", result.getName());
        assertEquals("Info", result.getInfo());
        verify(bookRepository, times(2)).findById(anyLong());
        verify(authorRepository).save(any(Author.class));
        verify(cache).clear();
    }

    @Test
    void createAuthor_WithEmptyBookList_CreatesAuthor() {
        AuthorCreateDto authorDto = new AuthorCreateDto("New Test Author",
                "Info", new ArrayList<>());
        Author savedAuthor = new Author(2L, authorDto.getName(),
                authorDto.getInfo(), Collections.emptySet());

        when(authorRepository.save(any(Author.class))).thenReturn(savedAuthor);

        AuthorGetDto result = authorService.createAuthor(authorDto);

        assertNotNull(result);
        assertEquals("New Test Author", result.getName());
        assertEquals("Info", result.getInfo());
        verify(bookRepository, times(0)).findById(anyLong());
        verify(authorRepository).save(any(Author.class));
        verify(cache).clear();
    }

    @Test
    void createAuthor_WithoutBookList_CreatesAuthor() {
        AuthorCreateDto authorDto = new AuthorCreateDto("New Test Author",
                "Info", null);
        Author savedAuthor = new Author(2L, authorDto.getName(),
                authorDto.getInfo(), Collections.emptySet());

        when(authorRepository.save(any(Author.class))).thenReturn(savedAuthor);

        AuthorGetDto result = authorService.createAuthor(authorDto);

        assertNotNull(result);
        assertEquals("New Test Author", result.getName());
        assertEquals("Info", result.getInfo());
        verify(bookRepository, times(0)).findById(anyLong());
        verify(authorRepository).save(any(Author.class));
        verify(cache).clear();
    }

    @Test
    void createAuthor_BookNotFound_ThrowsException() {
        AuthorCreateDto authorDto = new AuthorCreateDto("New Test Author",
                "Info", List.of(1L));

        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> authorService.createAuthor(authorDto));
        assertEquals("Book is not found with id: " + 1L, exception.getMessage());
        verify(bookRepository).findById(1L);
        verify(cache, never()).clear();
    }

    @Test
    void updateAuthor_WithValidAuthor_UpdatesAuthor() {
        Book book = new Book(2L, "Different Test Book", new HashSet<>(),
                null, 0, null, 0, null, null);

        AuthorCreateDto authorDto = new AuthorCreateDto("Updated Author",
                "Info", List.of(2L));


        when(authorRepository.findById(1L)).thenReturn(Optional.of(authorTest));
        when(bookRepository.findById(2L)).thenReturn(Optional.of(book));
        when(authorRepository.save(any(Author.class))).thenReturn(authorTest);

        AuthorGetDto result = authorService.updateAuthor(1L, authorDto);

        assertNotNull(result);
        assertEquals("Updated Author", result.getName());
        verify(authorRepository).findById(1L);
        verify(bookRepository).findById(2L);
        verify(authorRepository).save(any(Author.class));
        verify(cache).clear();
    }

    @Test
    void updateAuthor_WhenAuthorNotFound_ThrowsException() {
        AuthorCreateDto authorDto = new AuthorCreateDto("Updated Author",
                "Info", List.of(1L));

        when(authorRepository.findById(5L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> authorService.updateAuthor(5L, authorDto));
        assertEquals("Author is not found with id: " + 5L, exception.getMessage());
        verify(authorRepository).findById(5L);
    }

    @Test
    void updateAuthor_WhenBookNotFound_ThrowsException() {
        AuthorCreateDto authorDto = new AuthorCreateDto("Updated Author",
                "Info", List.of(20L));

        when(authorRepository.findById(1L)).thenReturn(Optional.of(authorTest));
        when(bookRepository.findById(20L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> authorService.updateAuthor(1L, authorDto));

        assertEquals("Book is not found with id: " + 20L, exception.getMessage());
        verify(authorRepository).findById(1L);
        verify(bookRepository).findById(20L);
    }

    @Test
    void deleteAuthor_WhenExisting_DeletesAuthor() {
        when(authorRepository.existsById(1L)).thenReturn(true);

        authorService.deleteAuthor(1L);

        verify(authorRepository).existsById(1L);
        verify(authorRepository).deleteById(1L);
        verify(cache).clear();
    }

    @Test
    void deleteAuthor_WhenNotFound_ThrowsException() {
        when(authorRepository.existsById(5L)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> authorService.deleteAuthor(5L));
        assertEquals("Author is not found with id: " + 5L, exception.getMessage());
        verify(authorRepository).existsById(5L);
        verify(authorRepository, never()).deleteById(5L);
    }
}