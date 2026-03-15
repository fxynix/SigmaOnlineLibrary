package library.dto.create;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRoleUpdateDto {
    @NotBlank(message = "Роль не может быть пустой")
    private String role;
}
