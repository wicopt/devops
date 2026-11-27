package maria.wicopt.user_service.dto.response;

import maria.wicopt.user_service.entity.User;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String firstName,
        String lastName,
        String email,
        String avatar,
        Instant lastSeenAt,
        Instant createdAt
) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getFirstName(),
                u.getLastName(), u.getEmail(), u.getAvatar(),
                u.getLastSeenAt(), u.getCreatedAt());
    }
}