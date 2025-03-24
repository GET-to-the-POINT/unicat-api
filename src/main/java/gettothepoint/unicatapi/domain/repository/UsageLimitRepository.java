package gettothepoint.unicatapi.domain.repository;

public interface UsageLimitRepository {
    int getUsage(Long memberId, String usageType);
    void incrementUsage(Long memberId, String usageType);
}
