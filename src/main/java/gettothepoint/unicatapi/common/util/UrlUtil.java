package gettothepoint.unicatapi.common.util;

import gettothepoint.unicatapi.common.propertie.AppProperties;
import org.springframework.web.util.UriComponentsBuilder;

public class UrlUtil {

    public static String buildBaseUrl(AppProperties.Api api) {
        return UriComponentsBuilder.newInstance()
                .scheme(api.protocol())
                .host(api.domain())
                .port(api.port())
                .build()
                .toUriString();
    }
}
