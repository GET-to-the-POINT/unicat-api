package gettothepoint.unicatapi.common.util;

import java.util.Base64;
import gettothepoint.unicatapi.common.propertie.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentUtil {

    private final AppProperties appProperties;

    public String createAuthorizationHeader() {
        String authString = appProperties.toss().secretKey() + ":";
        String encodedAuth = Base64.getEncoder().encodeToString(authString.getBytes());
        return "Basic " + encodedAuth;
    }
}