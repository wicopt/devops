package maria.wicopt.user_service.repository;

import maria.wicopt.user_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmail(String email);

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByEmail(String email);

    // Обновляем last_seen_at не чаще раза в минуту, не загружая сущность
    @Modifying
    @Query("""
            UPDATE User u SET u.lastSeenAt = :now
            WHERE u.id = :id
              AND (u.lastSeenAt IS NULL OR u.lastSeenAt < :threshold)
            """)
    int touchLastSeen(@Param("id") UUID id,
                      @Param("now") Instant now,
                      @Param("threshold") Instant threshold);
}