package library.dto.get;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryGetDto {
    private Long id;
    private String name;
    private Integer count;
}
