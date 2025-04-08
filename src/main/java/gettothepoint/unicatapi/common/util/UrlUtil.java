package gettothepoint.unicatapi.common.util;

import gettothepoint.unicatapi.common.properties.ApiProperties;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.util.UriComponentsBuilder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public  class UrlUtil {

    public static String buildBaseUrl(ApiProperties api) {
        return UriComponentsBuilder.newInstance()
                .scheme(api.protocol())
                .host(api.domain())
                .port(api.port())
                .build()
                .toUriString();
    }
}
