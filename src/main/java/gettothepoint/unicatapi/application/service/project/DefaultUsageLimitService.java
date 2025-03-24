package gettothepoint.unicatapi.application.service.project;

import gettothepoint.unicatapi.domain.repository.UsageLimitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DefaultUsageLimitService implements UsageLimitService {

    private final UsageLimitRepository usageLimitRepository;
    private static final String IMAGE = "image";
    private static final String SCRIPT = "script";
    private static final String PROJECT = "project";

    private final Map<String, Map<String, Integer>> limitMap = Map.of(
            "BASIC", Map.of(
                    IMAGE, 3,
                    SCRIPT, 3,
                    PROJECT, 2
            ),
            "PREMIUM", Map.of(
                    IMAGE, 10,
                    SCRIPT, 10,
                    PROJECT, 20
            ),
            "VIP", Map.of(
                    IMAGE, 50,
                    SCRIPT, 50,
                    PROJECT, 100
            )
    );

    @Override
    public void checkAndIncrement(Long memberId, String usageType, String plan) {
        Map<String, Integer> planLimits = limitMap.getOrDefault(plan.toUpperCase(), new HashMap<>());
        int allowed = planLimits.getOrDefault(usageType, 0);

        int currentUsage = usageLimitRepository.getUsage(memberId, usageType);
        if (currentUsage >= allowed) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    String.format("%s 플랜에서는 '%s'을(를) 최대 %d회 생성할 수 있습니다. 현재 %d회 생성함.",
                            plan, usageType, allowed, currentUsage)
            );
        }
        usageLimitRepository.incrementUsage(memberId, usageType);
    }
}
