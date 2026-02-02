package library.service;

import java.util.ArrayList;
import java.util.Optional;
import library.dto.create.ReviewCreateDto;
import library.dto.get.ReviewGetDto;
import library.exception.NotFoundException;
import library.model.Book;
import library.model.Review;
import library.model.User;
import library.repository.BookRepository;
import library.repository.ReviewRepository;
import library.repository.UserRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewService reviewService;

    private final User userTest = new User(1L, "Test User",
            "Password", "email@gmail.com", new ArrayList<>(), null);
    private final Book bookTest = new Book(1L, "Test Book",
            null, null, 100, new ArrayList<>(), 1000, null, null);
    private final Review reviewTest = new Review(1L, bookTest,
            userTest, 2, "Comment");

    @Test
    void getReviewById_WhenExists_ReturnsReview() {
        when(bookRepository.existsById(1L)).thenReturn(true);
        when(reviewRepository.findByIdAndBookId(1L, 1L)).thenReturn(Optional.of(reviewTest));

        ReviewGetDto result = reviewService.getReviewById(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Comment", result.getComment());
        assertEquals(2, result.getRating());
        verify(bookRepository).existsById(1L);
        verify(reviewRepository).findByIdAndBookId(1L, 1L);
    }

    @Test
    void getReviewById_WhenBookNotFound_ThrowsException() {
        when(bookRepository.existsById(20L)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> reviewService.getReviewById(1L, 20L));
        assertEquals("Book is not found with id: " + 20L, exception.getMessage());
        verify(bookRepository).existsById(20L);
        verify(reviewRepository, never()).findByIdAndBookId(anyLong(), anyLong());
    }

    @Test
    void getReviewById_WhenReviewNotFound_ThrowsException() {
        when(bookRepository.existsById(1L)).thenReturn(true);
        when(reviewRepository.findByIdAndBookId(20L, 1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> reviewService.getReviewById(20L, 1L));
        assertEquals("Review is not found with id: " + 20L, exception.getMessage());
        verify(bookRepository).existsById(1L);
        verify(reviewRepository).findByIdAndBookId(20L, 1L);
    }

    @Test
    void createReview_WithValidInput_CreatesReview() {
        ReviewCreateDto reviewDto = new ReviewCreateDto(1L, 5, "Great book");
        Review savedReview = new Review(2L, bookTest, userTest, 4, "New Comment");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(bookTest));
        when(userRepository.findById(1L)).thenReturn(Optional.of(userTest));
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        ReviewGetDto result = reviewService.createReview(1L, reviewDto);

        assertNotNull(result);
        assertEquals("New Comment", result.getComment());
        assertEquals(4, result.getRating());
        verify(bookRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void createReview_WhenBookNotFound_ThrowsException() {
        ReviewCreateDto reviewDto = new ReviewCreateDto(1L, 2, "New Comment");

        when(bookRepository.findById(20L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> reviewService.createReview(20L, reviewDto));
        assertEquals("Book is not found with id: " + 20L, exception.getMessage());
        verify(bookRepository).findById(20L);
        verify(userRepository, never()).findById(anyLong());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_WhenUserNotFound_ThrowsException() {
        ReviewCreateDto reviewDto = new ReviewCreateDto(20L, 2, "New Comment");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(bookTest));
        when(userRepository.findById(20L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> reviewService.createReview(1L, reviewDto));
        assertEquals("User is not found with id: " + 20L, exception.getMessage());
        verify(bookRepository).findById(1L);
        verify(userRepository).findById(20L);
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void updateReview_WithValidInput_UpdatesReview() {
        ReviewCreateDto reviewDto = new ReviewCreateDto(1L, 4, "Updated Comment");

        when(bookRepository.existsById(1L)).thenReturn(true);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(reviewTest));
        when(userRepository.findById(1L)).thenReturn(Optional.of(userTest));
        when(reviewRepository.save(any(Review.class))).thenReturn(reviewTest);

        ReviewGetDto result = reviewService.updateReview(1L, 1L, reviewDto);

        assertNotNull(result);
        assertEquals("Updated Comment", result.getComment());
        assertEquals(4, result.getRating());
        verify(bookRepository).existsById(1L);
        verify(reviewRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void updateReview_WhenBookNotFound_ThrowsException() {
        ReviewCreateDto reviewDto = new ReviewCreateDto(1L, 4, "Updated Comment");

        when(bookRepository.existsById(20L)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> reviewService.updateReview(1L, 20L, reviewDto));
        assertEquals("Book is not found with id: " + 20L, exception.getMessage());
        verify(bookRepository).existsById(20L);
        verify(reviewRepository, never()).findById(anyLong());
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void updateReview_WhenUserNotFound_ThrowsException() {
        ReviewCreateDto reviewDto = new ReviewCreateDto(20L, 4, "Updated Comment");

        when(bookRepository.existsById(1L)).thenReturn(true);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(reviewTest));
        when(userRepository.findById(20L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> reviewService.updateReview(1L, 1L, reviewDto));
        assertEquals("User is not found with id: " + 20L, exception.getMessage());
        verify(bookRepository).existsById(1L);
        verify(reviewRepository).findById(1L);
        verify(userRepository).findById(20L);
    }

    @Test
    void updateReview_WhenReviewNotFound_ThrowsException() {
        ReviewCreateDto reviewDto = new ReviewCreateDto(1L, 4, "Updated Comment");

        when(bookRepository.existsById(1L)).thenReturn(true);
        when(reviewRepository.findById(20L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> reviewService.updateReview(20L, 1L, reviewDto));
        assertEquals("Review is not found with id: " + 20L, exception.getMessage());
        verify(bookRepository).existsById(1L);
        verify(reviewRepository).findById(20L);
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void deleteReview_WhenReviewExists_DeletesReview() {
        when(bookRepository.existsById(1L)).thenReturn(true);
        when(reviewRepository.existsById(1L)).thenReturn(true);

        reviewService.deleteReview(1L, 1L);

        verify(bookRepository).existsById(1L);
        verify(reviewRepository).existsById(1L);
        verify(reviewRepository).deleteById(1L);
    }

    @Test
    void deleteReview_WhenBookNotFound_ThrowsException() {
        when(bookRepository.existsById(20L)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> reviewService.deleteReview(1L, 20L));
        assertEquals("Book is not found with id: " + 20L, exception.getMessage());
        verify(bookRepository).existsById(20L);
        verify(reviewRepository, never()).existsById(anyLong());
    }

    @Test
    void deleteReview_WhenReviewNotFound_ThrowsException() {
        when(bookRepository.existsById(1L)).thenReturn(true);
        when(reviewRepository.existsById(20L)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> reviewService.deleteReview(20L, 1L));
        assertEquals("Review is not found with id: " + 20L, exception.getMessage());
        verify(bookRepository).existsById(1L);
        verify(reviewRepository).existsById(20L);
        verify(reviewRepository, never()).deleteById(anyLong());
    }
}