package taeniverse.unicatApi.component.oauth2;

import java.util.Map;

public interface OAuth2UserInfo {
    String getId();
    String getEmail();
    String getName();
    Map<String, Object> getAttributes();
}