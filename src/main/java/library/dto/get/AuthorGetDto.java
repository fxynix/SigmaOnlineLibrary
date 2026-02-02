package library.dto.get;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthorGetDto {
    private Long id;
    private String name;
    private String info;
    private List<String> books;
}
