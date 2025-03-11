package gettothepoint.unicatapi.common.util;

import gettothepoint.unicatapi.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class EmailValidator {

    private final MemberRepository memberRepository;
    private final MessageSource messageSource;

    public void validateEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            String errorMessage = messageSource.getMessage("error.email.in.use", null, "", LocaleContextHolder.getLocale());
            throw new ResponseStatusException(HttpStatus.CONFLICT, errorMessage);
        }
    }
}
