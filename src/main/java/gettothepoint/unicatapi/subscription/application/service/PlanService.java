package gettothepoint.unicatapi.subscription.application.service;

import gettothepoint.unicatapi.subscription.domain.entity.Plan;
import gettothepoint.unicatapi.subscription.domain.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;

    @Transactional(readOnly = true)
    public Plan getPlanByName(String name) {
        return planRepository.findByName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found: " + name));
    }
    
    @Transactional(readOnly = true)
    public Plan getBasicPlan() {
        return planRepository.findByName("BASIC")
                .orElseThrow(() -> new IllegalStateException("기본 플랜이 존재하지 않습니다."));
    }

    public Plan getOrElseThrow(Long planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found with id: " + planId));
    }
}
