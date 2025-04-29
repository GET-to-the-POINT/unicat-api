package gettothepoint.unicatapi.subscription.persistence;

import gettothepoint.unicatapi.subscription.domain.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 구독 플랜 정보 관련 데이터 액세스를 위한 리포지토리
 */
@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    
    /**
     * 플랜 이름으로 플랜 정보 조회
     * 
     * @param name 플랜 이름
     * @return 플랜 정보
     */
    Optional<Plan> findByName(String name);
}
