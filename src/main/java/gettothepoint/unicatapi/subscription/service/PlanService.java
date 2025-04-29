package gettothepoint.unicatapi.subscription.service;

import gettothepoint.unicatapi.subscription.entity.Plan;
import gettothepoint.unicatapi.subscription.persistence.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;
    
    @Transactional(readOnly = true)
    public Plan getBasicPlan() {
        return planRepository.findByName("BASIC")
                .orElseThrow(() -> new IllegalStateException("기본 플랜이 존재하지 않습니다."));
    }
}
