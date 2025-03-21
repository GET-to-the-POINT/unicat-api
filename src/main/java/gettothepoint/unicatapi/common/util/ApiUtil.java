package gettothepoint.unicatapi.common.util;

import gettothepoint.unicatapi.common.propertie.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
@RequiredArgsConstructor
public class ApiUtil {

    private final AppProperties appProperties;

    public String encodeSecretKey() {
        String secretKeyWithColon = appProperties.toss().secretKey() + ":";
        return Base64.getEncoder().encodeToString(secretKeyWithColon.getBytes());
    }

    public HttpHeaders createHeaders(String encodedSecretKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedSecretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
