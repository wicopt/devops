package maria.wicopt.user_service.service;

import lombok.RequiredArgsConstructor;
import maria.wicopt.user_service.entity.RefreshToken;
import maria.wicopt.user_service.repository.RefreshTokenRepository;
import maria.wicopt.user_service.security.JwtProperties;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final RefreshTokenRepository repository;
    private final JwtProperties props;

    /** Новая сессия: новый family_id. Возвращает сырой токен (в базе лежит только хэш). */
    public String issueForNewSession(UUID userId) {
        return issue(userId, UUID.randomUUID());
    }

    String issue(UUID userId, UUID familyId) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        RefreshToken entity = new RefreshToken();
        entity.setTokenHash(hash(rawToken));
        entity.setUserId(userId);
        entity.setFamilyId(familyId);
        entity.setExpiresAt(Instant.now().plus(props.refreshTtl()));
        repository.save(entity);

        return rawToken;
    }

    static byte[] hash(String rawToken) {
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}