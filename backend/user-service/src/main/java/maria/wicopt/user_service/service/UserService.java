package maria.wicopt.user_service.service;

import lombok.RequiredArgsConstructor;
import maria.wicopt.user_service.dto.response.UserResponse;
import maria.wicopt.user_service.exception.ApiException;
import maria.wicopt.user_service.exception.ErrorCode;
import maria.wicopt.user_service.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /** Текущий пользователь: id берётся из subject access-токена. */
    @Transactional(readOnly = true)
    public UserResponse getMe(UUID currentUserId) {
        return getById(currentUserId);
    }

    /** Пользователь по id, 404 если не найден. */
    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        return userRepository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    }
}