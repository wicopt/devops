package maria.wicopt.user_service.repository;

import maria.wicopt.user_service.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, byte[]> {
}