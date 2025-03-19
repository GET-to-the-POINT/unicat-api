package gettothepoint.unicatapi.common.validation.email;


import gettothepoint.unicatapi.application.service.member.MemberService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    private final MemberService memberService;

    @Autowired
    public UniqueEmailValidator(MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {

        if (email == null || email.isEmpty()) {
            return true;
        }
        return !memberService.isEmailTaken(email);
    }
}
