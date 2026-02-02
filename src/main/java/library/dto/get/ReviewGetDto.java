package library.dto.get;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewGetDto {
    private Long id;
    private UserGetDto user;
    private int rating;
    private String comment;
}
