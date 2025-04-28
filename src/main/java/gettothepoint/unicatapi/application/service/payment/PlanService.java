package gettothepoint.unicatapi.application.service.payment;

import gettothepoint.unicatapi.subscription.domain.entity.Plan;
import gettothepoint.unicatapi.domain.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;

    @Transactional
    public Plan getPlanByName(String name) {
        return planRepository.findByName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found: " + name));
    }
}
