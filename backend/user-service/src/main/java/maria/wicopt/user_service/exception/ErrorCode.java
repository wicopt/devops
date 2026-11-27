package maria.wicopt.user_service.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Validation failed"),
    MALFORMED_REQUEST(HttpStatus.BAD_REQUEST, "Malformed request"),
    CANNOT_FRIEND_SELF(HttpStatus.BAD_REQUEST, "Cannot send a friend request to yourself"),

    // 401 / 403
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Authentication required"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid username or password"),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Access denied"),

    // 404
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    FRIEND_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "Friend request not found"),

    // 409
    USERNAME_TAKEN(HttpStatus.CONFLICT, "Username is already taken"),
    EMAIL_TAKEN(HttpStatus.CONFLICT, "Email is already registered"),
    FRIEND_REQUEST_EXISTS(HttpStatus.CONFLICT, "Friendship or request already exists"),
    CONFLICT(HttpStatus.CONFLICT, "Conflict"),

    // 500
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");

    private final HttpStatus status;
    private final String title;
}