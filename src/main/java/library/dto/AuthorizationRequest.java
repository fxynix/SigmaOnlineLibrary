package library.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthorizationRequest {
    @NotBlank(message = "Email can't be blank")
    @Email(message = "Invalid email")
    private String email;
    private String password;
}
