package gettothepoint.unicatapi.domain.repository;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Repository
public class LocalUsageLimitRepository implements UsageLimitRepository {

    private final Map<Long, Map<String, Integer>> usageStore = new ConcurrentHashMap<>();

    @Override
    public int getUsage(Long memberId, String usageType) {
        return usageStore
                .getOrDefault(memberId, new ConcurrentHashMap<>())
                .getOrDefault(usageType, 0);
    }

    @Override
    public void incrementUsage(Long memberId, String usageType) {
        usageStore.computeIfAbsent(memberId, k -> new ConcurrentHashMap<>())
                .merge(usageType, 1, Integer::sum);
    }

}
