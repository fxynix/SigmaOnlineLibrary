package library.service;

import jakarta.transaction.Transactional;
import java.util.List;
import library.cache.InMemoryCache;
import library.dto.create.ReviewCreateDto;
import library.dto.get.ReviewGetDto;
import library.exception.AuthenticationException;
import library.exception.ConflictException;
import library.exception.DuplicateReviewException;
import library.exception.NotFoundException;
import library.mapper.ReviewMapper;
import library.model.Book;
import library.model.Review;
import library.model.Role;
import library.model.User;
import library.repository.BookRepository;
import library.repository.ReviewRepository;
import library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final InMemoryCache cache;
    private static final String BOOK_NOT_FOUND_MESSAGE = "Book is not found with id: ";
    private static final String REVIEW_NOT_FOUND_MESSAGE = "Review is not found with id: ";
    private static final String USER_NOT_FOUND_MESSAGE = "User is not found with id: ";

    @Autowired
    public ReviewService(ReviewRepository reviewRepository,
                         BookRepository bookRepository,
                         UserRepository userRepository, InMemoryCache cache) {
        this.reviewRepository = reviewRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.cache = cache;
    }

    public List<ReviewGetDto> getAllReviews(Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new NotFoundException(BOOK_NOT_FOUND_MESSAGE + bookId);
        }

        return reviewRepository.findByBookId(bookId).stream()
                .map(ReviewMapper::toDto)
                .toList();
    }

    public ReviewGetDto getReviewById(Long id, Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new NotFoundException(BOOK_NOT_FOUND_MESSAGE + bookId);
        }
        Review review = reviewRepository.findByIdAndBookId(id, bookId)
                .orElseThrow(() -> new NotFoundException(REVIEW_NOT_FOUND_MESSAGE + id));
        return ReviewMapper.toDto(review);
    }

    @Transactional
    public ReviewGetDto createReview(Long bookId, ReviewCreateDto reviewDto) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NotFoundException(BOOK_NOT_FOUND_MESSAGE + bookId));

        if (reviewDto.getUserId() == null) {
            throw new AuthenticationException("User is not logged in");
        }

        User user = userRepository.findById(reviewDto.getUserId())
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_MESSAGE + reviewDto.getUserId()));

        boolean exists = reviewRepository.existsByBookIdAndUserId(bookId, user.getId());
        if (exists) {
            throw new DuplicateReviewException("Пользователь уже оставил отзыв на эту книгу");
        }
        Review review = ReviewMapper.fromDto(reviewDto);

        book.getReviews().add(review);
        review.setBook(book);

        user.getReviews().add(review);
        review.setUser(user);

        recalculateBookRating(book);

        cache.clear();
        return ReviewMapper.toDto(reviewRepository.save(review));
    }

    @Transactional
    public ReviewGetDto updateReview(Long id, Long bookId, ReviewCreateDto reviewDto) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(REVIEW_NOT_FOUND_MESSAGE + id));

        checkOwnershipOrAdmin(review);

        review.setComment(reviewDto.getComment());
        review.setRating(reviewDto.getRating());

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NotFoundException(BOOK_NOT_FOUND_MESSAGE + bookId));

        recalculateBookRating(book);

        cache.clear();
        return ReviewMapper.toDto(reviewRepository.save(review));
    }

    public void deleteReview(Long id, Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NotFoundException(BOOK_NOT_FOUND_MESSAGE + bookId));

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(REVIEW_NOT_FOUND_MESSAGE + id));

        checkOwnershipOrAdmin(review);

        reviewRepository.deleteById(id);

        recalculateBookRating(book);

        cache.clear();
    }

    private void checkOwnershipOrAdmin(Review review) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return;

        String currentEmail = auth.getName();
        User currentUser = userRepository.findByEmail(currentEmail);

        if (currentUser != null && currentUser.getRole() != Role.ADMIN && !review.getUser().getId().equals(currentUser.getId())) {
            throw new ConflictException("Вы можете изменять только свои отзывы");
        }
    }

    private void recalculateBookRating(Book book) {
        List<Review> reviews = reviewRepository.findByBookId(book.getId());
        double newRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
        book.setRating(newRating);
        bookRepository.save(book); // Сохраняем обновлённый рейтинг
    }
}
