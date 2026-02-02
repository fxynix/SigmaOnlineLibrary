package library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import library.dto.create.ReviewCreateDto;
import library.dto.get.ReviewGetDto;
import library.service.ReviewService;
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
@RequestMapping("/api/books/{bookId}/reviews")
@Tag(name = "Review", description = "API for managing reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Operation(summary = "Get all reviews", description = "Retrieves all reviews of specified book")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @GetMapping
    public ResponseEntity<List<ReviewGetDto>> getAllReviews(
            @Parameter(description = "Book's ID", example = "2") @PathVariable Long bookId) {
        return ResponseEntity.ok(reviewService.getAllReviews(bookId));
    }

    @Operation(summary = "Get book's review by ID",
            description = "Retrieves existing review of book")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Review retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Book or review not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReviewGetDto> getReviewById(
            @Parameter(description = "Review's ID", example = "2") @PathVariable Long id,
            @Parameter(description = "Book's ID", example = "2") @PathVariable Long bookId) {
        return ResponseEntity.ok(reviewService.getReviewById(id, bookId));
    }

    @Operation(summary = "Create book's review",
            description = "Creates review for book")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Review created successfully"),
        @ApiResponse(responseCode = "400", description = "Incorrect entered data"),
        @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @PostMapping
    public ResponseEntity<ReviewGetDto> createReview(
            @Parameter(description = "Book's ID", example = "2") @PathVariable Long bookId,
            @Parameter(description = "Data to create review")
            @Valid @RequestBody ReviewCreateDto review) {
        return new ResponseEntity<>(reviewService.createReview(bookId, review), HttpStatus.CREATED);
    }

    @Operation(summary = "Update book's review by ID",
            description = "Update existing review of book")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Review updated successfully"),
        @ApiResponse(responseCode = "400", description = "Incorrect entered data"),
        @ApiResponse(responseCode = "404", description = "Book or review not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ReviewGetDto> updateReview(
            @Parameter(description = "Review's ID", example = "2") @PathVariable Long id,
            @Parameter(description = "Book's ID", example = "2") @PathVariable Long bookId,
            @Parameter(description = "Data to update review")
            @Valid @RequestBody ReviewCreateDto review) {
        return ResponseEntity.ok(reviewService.updateReview(id, bookId, review));
    }

    @Operation(summary = "Delete book's review by ID",
            description = "Delete existing review of book")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Review deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Book or review not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
            @Parameter(description = "Review's ID", example = "2") @PathVariable Long id,
            @Parameter(description = "Book's ID", example = "2") @PathVariable Long bookId) {
        reviewService.deleteReview(id, bookId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
