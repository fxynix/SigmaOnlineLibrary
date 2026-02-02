package library.dto.create;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BulkCreateDto<T> {
    @NotNull(message = "You should provide at least 1 book")
    @Size(min = 1, message = "You should provide at least 1 book")
    private List<T> dtos;
}
