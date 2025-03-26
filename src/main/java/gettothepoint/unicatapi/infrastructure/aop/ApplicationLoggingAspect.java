package gettothepoint.unicatapi.infrastructure.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ApplicationLoggingAspect {

    @Pointcut("within(gettothepoint.unicatapi.application..*)")
    public void applicationPackagePointcut() {}

    @Around("applicationPackagePointcut()")
    public Object logServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();

        log.info("🚀 Service START: {}", methodName);
        try {
            Object result = joinPoint.proceed();
            log.info("✅ Service END  : {}", methodName);
            return result;
        } catch (Throwable t) {
            log.info("❌ Service END (Exception): {}", methodName);
            throw t;
        }
    }
}