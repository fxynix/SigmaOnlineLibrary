package library.dto.get;

import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookGetDto {
    private Long id;
    private String name;
    private Set<AuthorGetDto> authors;
    private Set<CategoryGetDto> categories;
    private int pageAmount;
    private List<ReviewGetDto> reviews;
    private int year;
    private Double rating;
}
