package library.service;

import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookService {
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final InMemoryCache cache;
    private static final String AUTHOR_NOT_FOUND_MESSAGE = "Author is not found with id: ";
    private static final String BOOK_NOT_FOUND_MESSAGE = "Book is not found with id: ";
    private static final String CATEGORY_NOT_FOUND_MESSAGE = "Category is not found with id: ";

    @Autowired
    public BookService(BookRepository bookRepository,
                       AuthorRepository authorRepository,
                       CategoryRepository categoryRepository, InMemoryCache cache) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
        this.cache = cache;
    }

    public List<BookGetDto> getAllBooks() {
        return bookRepository.findAll().stream().map(BookMapper::toDto).toList();
    }

    public BookGetDto getBookById(Long id) {
        String key = "book_by_id_" + id;
        if (cache.containsKey(key)) {
            return (BookGetDto) cache.get(key);
        }
        BookGetDto book = BookMapper.toDto(bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(BOOK_NOT_FOUND_MESSAGE + id)));
        cache.put(key, book);
        return book;
    }

    public List<BookGetDto> getBookByName(String name)  {
        String key = "books_by_name_" + name;
        if (cache.containsKey(key)) {
            return (List<BookGetDto>) cache.get(key);
        }

        List<BookGetDto> booksDto = bookRepository.findByName(name).stream()
                .map(BookMapper::toDto).toList();
        if (booksDto.isEmpty()) {
            throw new NotFoundException("Book not found with name:" + name);
        }
        cache.put(key, booksDto);
        return booksDto;
    }

    public List<BookGetDto> getBookByAuthor(String name)  {
        String key = "books_by_author_" + name;
        if (cache.containsKey(key)) {
            return (List<BookGetDto>) cache.get(key);
        }

        List<BookGetDto> booksDto = bookRepository.findByAuthor(name).stream()
                .map(BookMapper::toDto).toList();
        if (booksDto.isEmpty()) {
            throw new NotFoundException("Book not found with author's name: " + name);
        }
        cache.put(key, booksDto);
        return booksDto;
    }

    public List<BookGetDto> getBookByCategory(String name) {
        String key = "books_by_category_" + name;
        if (cache.containsKey(key)) {
            return (List<BookGetDto>) cache.get(key);
        }

        List<BookGetDto> booksDto = bookRepository.findByCategory(name).stream()
                .map(BookMapper::toDto).toList();
        if (booksDto.isEmpty()) {
            throw new NotFoundException("Book not found with category's name: " + name);
        }
        cache.put(key, booksDto);
        return booksDto;
    }

    @Transactional
    public BookGetDto createBook(BookCreateDto bookDto) {
        Book bookEntity = BookMapper.fromDto(bookDto);

        Set<Author> authors = new HashSet<>();
        if (bookDto.getAuthorIds() != null && !bookDto.getAuthorIds().isEmpty()) {
            authors = bookDto.getAuthorIds().stream()
                    .map(authorId -> authorRepository.findById(authorId)
                    .orElseThrow(() -> new NotFoundException(AUTHOR_NOT_FOUND_MESSAGE + authorId)))
                    .collect(Collectors.toSet());
        }

        Set<Category> categories = new HashSet<>();
        if (bookDto.getCategoryIds() != null && !bookDto.getCategoryIds().isEmpty()) {
            categories = bookDto.getCategoryIds().stream()
                    .map(categoryId -> categoryRepository.findById(categoryId)
                    .orElseThrow(()
                            -> new NotFoundException(CATEGORY_NOT_FOUND_MESSAGE + categoryId)))
                    .collect(Collectors.toSet());
        }
        bookEntity.setCategories(categories);
        bookEntity.setAuthors(authors);
        cache.clear();

        return BookMapper.toDto(bookRepository.save(bookEntity));
    }

    @Transactional
    public BookGetDto updateBook(Long id, BookCreateDto bookDto) {
        Book bookEntity = bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(BOOK_NOT_FOUND_MESSAGE + id));
        bookEntity.setName(bookDto.getName());
        bookEntity.setYear(bookDto.getYear());
        bookEntity.setPageAmount(bookDto.getPageAmount());

        Set<Author> authors = new HashSet<>();
        Set<Category> categories = new HashSet<>();
        if (bookDto.getAuthorIds() != null && !bookDto.getAuthorIds().isEmpty()) {
            bookDto.getAuthorIds().forEach(authorId -> {
                Author author = authorRepository.findById(authorId)
                        .orElseThrow(()
                                -> new NotFoundException(AUTHOR_NOT_FOUND_MESSAGE + authorId));
                authors.add(author);
            });
        }
        bookEntity.setAuthors(authors);

        if (bookDto.getCategoryIds() != null && !bookDto.getCategoryIds().isEmpty()) {
            bookDto.getCategoryIds().forEach(categoryId -> {
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(()
                                -> new NotFoundException(CATEGORY_NOT_FOUND_MESSAGE + categoryId));
                categories.add(category);
            });
        }
        bookEntity.setCategories(categories);
        cache.clear();

        return BookMapper.toDto(bookRepository.save(bookEntity));
    }

    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new NotFoundException(BOOK_NOT_FOUND_MESSAGE + id);
        }
        cache.clear();
        bookRepository.deleteById(id);
    }
}
