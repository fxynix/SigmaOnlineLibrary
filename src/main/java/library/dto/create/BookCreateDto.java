package library.dto.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookCreateDto {
    @NotBlank(message = "Book's name can't be blank")
    @Size(max = 255, message = "Book name must be less than 256 characters")
    private String name;
    private Set<Long> authorIds;
    private Set<Long> categoryIds;
    @NotNull(message = "Book's page amount can't be blank")
    private int pageAmount;
    private int year;
}
