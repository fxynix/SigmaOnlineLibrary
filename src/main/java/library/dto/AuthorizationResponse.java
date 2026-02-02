package library.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthorizationResponse {
    private String token;
    private Long userId;
    private String name;
    private String email;
}
