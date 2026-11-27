package maria.wicopt.user_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9_.-]{3,100}$",
                 message = "3-100 chars: letters, digits, '_', '.', '-'")
        String username,

        @NotBlank @Size(max = 100)
        String firstName,

        @Size(max = 100)
        String lastName,

        @NotBlank @Email @Size(max = 100)
        String email,

        @NotBlank @Size(min = 8, max = 72)
        String password
) {}