package library.mapper;

import library.dto.create.ReviewCreateDto;
import library.dto.get.ReviewGetDto;
import library.model.Review;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ReviewMapper {
    public ReviewGetDto toDto(Review review) {
        ReviewGetDto dto = new ReviewGetDto();

        dto.setId(review.getId());
        dto.setUser(UserMapper.toDto(review.getUser()));
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());

        return dto;
    }

    public Review fromDto(ReviewCreateDto reviewDto) {
        Review entity = new Review();

        entity.setRating(reviewDto.getRating());
        entity.setComment(reviewDto.getComment());

        return entity;
    }
}
