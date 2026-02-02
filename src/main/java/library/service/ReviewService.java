package library.service;

import jakarta.transaction.Transactional;
import java.util.List;
import library.cache.InMemoryCache;
import library.dto.create.ReviewCreateDto;
import library.dto.get.ReviewGetDto;
import library.exception.AuthenticationException;
import library.exception.NotFoundException;
import library.mapper.ReviewMapper;
import library.model.Book;
import library.model.Review;
import library.model.User;
import library.repository.BookRepository;
import library.repository.ReviewRepository;
import library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
                .orElseThrow(()
                        -> new NotFoundException(USER_NOT_FOUND_MESSAGE + reviewDto.getUserId()));

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

        review.setComment(reviewDto.getComment());
        review.setRating(reviewDto.getRating());

        if (reviewDto.getUserId() == null) {
            throw new AuthenticationException("User is not logged in");
        }

        User user = userRepository.findById(reviewDto.getUserId())
                .orElseThrow(()
                        -> new NotFoundException(USER_NOT_FOUND_MESSAGE + reviewDto.getUserId()));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NotFoundException(BOOK_NOT_FOUND_MESSAGE + bookId));

        user.getReviews().add(review);
        review.setUser(user);

        recalculateBookRating(book);

        cache.clear();
        return ReviewMapper.toDto(reviewRepository.save(review));
    }

    public void deleteReview(Long id, Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NotFoundException(BOOK_NOT_FOUND_MESSAGE + bookId));

        if (!reviewRepository.existsById(id)) {
            throw new NotFoundException(REVIEW_NOT_FOUND_MESSAGE + id);
        }

        reviewRepository.deleteById(id);

        recalculateBookRating(book);

        cache.clear();
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
