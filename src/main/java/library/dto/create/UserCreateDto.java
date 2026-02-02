package library.dto.create;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateDto {
    @NotBlank(message = "User's name can't be blank")
    @Size(max = 100, message = "Username must be less than 101 characters")
    private String name;
    @NotBlank(message = "Email can't be blank")
    @Email(message = "Invalid email")
    private String email;
    private String password;
}
