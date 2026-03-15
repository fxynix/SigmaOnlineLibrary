package library.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthorizationRequest {
    @NotBlank(message = "Логин не может быть пустым")
    private String login;

    private String password;
}
