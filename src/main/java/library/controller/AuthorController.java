package library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import library.dto.create.AuthorCreateDto;
import library.dto.get.AuthorGetDto;
import library.service.AuthorService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/authors")
@Tag(name = "Author", description = "API for managing authors")
public class AuthorController {
    private final AuthorService authorService;

    @Autowired
    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @Operation(summary = "Get author by ID", description = "Retrieves author by his ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Author retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Author not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AuthorGetDto> getAuthorById(
            @Parameter(description = "Author's ID", example = "2") @PathVariable Long id) {
        return ResponseEntity.ok(authorService.getAuthorById(id));
    }

    @Operation(summary = "Get all authors", description = "Retrieves all authors")
    @ApiResponse(responseCode = "200", description = "Authors retrieved successfully")
    @GetMapping
    public ResponseEntity<List<AuthorGetDto>> getAllAuthors() {
        return ResponseEntity.ok(authorService.getAllAuthors());
    }

    @Operation(summary = "Create author", description = "Creates new author")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Author created successfully"),
        @ApiResponse(responseCode = "400", description = "Incorrect entered data")
    })
    @PostMapping
    public ResponseEntity<AuthorGetDto> createAuthor(
            @Parameter(description = "Data to create author")
            @Valid @RequestBody AuthorCreateDto author) {
        return new ResponseEntity<>(authorService.createAuthor(author), HttpStatus.CREATED);
    }

    @Operation(summary = "Update author by ID", description = "Update existing author")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Author updated successfully"),
        @ApiResponse(responseCode = "400", description = "Incorrect entered data"),
        @ApiResponse(responseCode = "404", description = "Author not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<AuthorGetDto> updateAuthor(
            @Parameter(description = "Author's ID", example = "2") @PathVariable Long id,
            @Parameter(description = "Data to update author")
            @Valid @RequestBody AuthorCreateDto author) {
        return ResponseEntity.ok(authorService.updateAuthor(id, author));
    }

    @Operation(summary = "Delete author by ID", description = "Delete existing author")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Author deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Author not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(
            @Parameter(description = "Author's ID", example = "2") @PathVariable Long id) {
        authorService.deleteAuthor(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
