package gettothepoint.unicatapi.application.service.project;

public interface UsageLimitService {
    void checkAndIncrement(Long memberId, String usageType, String plan);
}
