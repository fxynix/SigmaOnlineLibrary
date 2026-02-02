package library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import library.dto.create.BookCreateDto;
import library.dto.create.BulkCreateDto;
import library.dto.get.BookGetDto;
import library.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Book", description = "API for managing books")
public class BookController {
    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @Operation(summary = "Get book by ID", description = "Retrieves book by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Book retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookGetDto> getBookById(
            @Parameter(description = "Book's ID", example = "2") @PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @Operation(summary = "Get books by author",
            description = "Retrieves book's list by author's name")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Book found successfully"),
        @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @GetMapping("/search/author")
    public ResponseEntity<List<BookGetDto>> getBookByAuthor(
            @Parameter(description = "Author's name", example = "Юваль Ной Харари")
            @RequestParam String name) {
        return ResponseEntity.ok(bookService.getBookByAuthor(name));
    }

    @Operation(summary = "Get books by category",
            description = "Retrieves book's list by category's name")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Book found successfully"),
        @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @GetMapping("/search/category")
    public ResponseEntity<List<BookGetDto>> getBookByCategory(
            @Parameter(description = "Category's name", example = "Бестселлер")
            @RequestParam String name) {
        return ResponseEntity.ok(bookService.getBookByCategory(name));
    }

    @Operation(summary = "Get books by name",
            description = "Retrieves book's list by its name")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Book found successfully"),
        @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @GetMapping("/search")
    public ResponseEntity<List<BookGetDto>> getBookByName(
            @Parameter(description = "Book's name", example = "Библия") @RequestParam String name) {
        return ResponseEntity.ok(bookService.getBookByName(name));
    }

    @Operation(summary = "Get all books", description = "Retrieves all books")
    @ApiResponse(responseCode = "200", description = "Book found successfully")
    @GetMapping
    public ResponseEntity<List<BookGetDto>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @Operation(summary = "Create book", description = "Creates new book")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Book created successfully"),
        @ApiResponse(responseCode = "400", description = "Incorrect entered data")
    })
    @PostMapping
    public ResponseEntity<BookGetDto> createBook(@Parameter(description = "Data to create book")
                                                     @Valid @RequestBody BookCreateDto book) {
        return new ResponseEntity<>(bookService.createBook(book), HttpStatus.CREATED);
    }

    @Operation(summary = "Create many books", description = "Creates many books at once")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Books created successfully"),
        @ApiResponse(responseCode = "400", description = "Incorrect entered data")
    })
    @PostMapping("/bulk")
    public ResponseEntity<List<BookGetDto>> createBooks(
            @Parameter(description = "Data to create books")
            @Valid @RequestBody
            BulkCreateDto<BookCreateDto> books) {
        return new ResponseEntity<>(books.getDtos().stream().map(
                bookCreateDto -> bookService.createBook(bookCreateDto))
                .toList(), HttpStatus.CREATED);
    }

    @Operation(summary = "Update book by ID", description = "Update existing book")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Book updated successfully"),
        @ApiResponse(responseCode = "400", description = "Incorrect entered data"),
        @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<BookGetDto> updateBook(
            @Parameter(description = "Book's ID", example = "2") @PathVariable Long id,
            @Parameter(description = "Data to update book")
            @Valid @RequestBody BookCreateDto book) {
        return ResponseEntity.ok(bookService.updateBook(id, book));
    }

    @Operation(summary = "Delete book by ID", description = "Delete existing book")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Book updated successfully"),
        @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@Parameter(description = "Book's ID", example = "2")
                                               @PathVariable Long id) {
        bookService.deleteBook(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
