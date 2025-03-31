package gettothepoint.unicatapi.common.validation.usagelimit;

import gettothepoint.unicatapi.application.service.project.UsageLimitService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Aspect
@Component
@RequiredArgsConstructor
public class UsageLimitValidator {

    private final UsageLimitService usageLimitService;
    private static final String IMAGE = "image";
    private static final String SCRIPT = "script";

    @Around("@annotation(usageLimit)")
    public Object enforceUsageLimit(ProceedingJoinPoint joinPoint, UsageLimit usageLimit) throws Throwable {

        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long memberId = Long.valueOf(jwt.getSubject());
        String plan = jwt.getClaim("plan");

        String usageType;

        if (!usageLimit.value().isBlank()) {
            usageType = usageLimit.value();
            usageLimitService.checkAndIncrement(memberId, usageType, plan);
        } else {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] parameterNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();
            String typeValue = null;
            for (int i = 0; i < parameterNames.length; i++) {
                if ("type".equals(parameterNames[i]) && args[i] instanceof String string) {
                    typeValue = string;
                    break;
                }
            }

            if (typeValue == null || typeValue.isBlank()) {
                usageLimitService.checkAndIncrement(memberId, IMAGE, plan);
                usageLimitService.checkAndIncrement(memberId, SCRIPT, plan);
            } else if (IMAGE.equalsIgnoreCase(typeValue)) {
                usageLimitService.checkAndIncrement(memberId, IMAGE, plan);
            } else if (SCRIPT.equalsIgnoreCase(typeValue)) {
                usageLimitService.checkAndIncrement(memberId, SCRIPT, plan);
            } else if ("artifact".equalsIgnoreCase(typeValue)) {
                usageLimitService.checkAndIncrement(memberId, "project", plan);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid type parameter: " + typeValue);
            }
        }

        return joinPoint.proceed();
    }
}