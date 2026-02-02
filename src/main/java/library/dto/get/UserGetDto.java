package library.dto.get;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserGetDto {
    private Long id;
    private String name;
    private String email;
}
