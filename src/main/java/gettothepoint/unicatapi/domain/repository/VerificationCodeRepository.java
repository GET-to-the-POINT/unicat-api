package gettothepoint.unicatapi.domain.repository;

import gettothepoint.unicatapi.domain.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode,Long> {
    Optional<VerificationCode> findByCode(String code);
}
