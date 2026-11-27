package maria.wicopt.user_service.service;

import lombok.RequiredArgsConstructor;
import maria.wicopt.user_service.dto.request.LoginRequest;
import maria.wicopt.user_service.dto.request.RegisterRequest;
import maria.wicopt.user_service.dto.response.TokenResponse;
import maria.wicopt.user_service.dto.response.UserResponse;
import maria.wicopt.user_service.entity.User;
import maria.wicopt.user_service.exception.ApiException;
import maria.wicopt.user_service.exception.ErrorCode;
import maria.wicopt.user_service.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private static final String DUMMY_HASH = "$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5BHoRJvYiXk2Oq3pP1dQ0nQ1y5y6K";

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new ApiException(ErrorCode.USERNAME_TAKEN);
        }
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(ErrorCode.EMAIL_TAKEN);
        }

        User user = new User();
        user.setUsername(request.username());
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName() == null ? null : request.lastName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        // saveAndFlush, чтобы нарушение уникальности вылетело здесь, а не при коммите
        user = userRepository.saveAndFlush(user);

        return issueTokens(user);
    }

     @Transactional
    public TokenResponse login(LoginRequest request) {
        String login = request.login().trim();
        Optional<User> found = login.contains("@") 
            ? userRepository.findByEmail(login.toLowerCase(Locale.ROOT)) 
            : userRepository.findByUsernameIgnoreCase(login);
        
        User user = found.orElse(null);
        String hash = user != null ? user.getPasswordHash() : DUMMY_HASH;

        boolean matches = passwordEncoder.matches(request.password(), hash);
         if (user == null || !matches) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS);
        }
        return issueTokens(user);
    }
    private TokenResponse issueTokens(User user) {
        return new TokenResponse(
                jwtService.generateAccessToken(user),
                refreshTokenService.issueForNewSession(user.getId()),
                "Bearer",
                jwtService.accessTtlSeconds(),
                UserResponse.from(user));
    }
}