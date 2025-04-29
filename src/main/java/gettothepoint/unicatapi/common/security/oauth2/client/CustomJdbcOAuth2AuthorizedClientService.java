package gettothepoint.unicatapi.common.security.oauth2.client;

import gettothepoint.unicatapi.common.security.oauth2.client.authorizedclient.VendorOAuth2AuthorizedClientHandler;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;

import static gettothepoint.unicatapi.common.security.oauth2.client.authorizedclient.VendorOAuth2AuthorizedClientHandler.getHandler;


@Service
public class CustomJdbcOAuth2AuthorizedClientService implements OAuth2AuthorizedClientService {

    private final JdbcOAuth2AuthorizedClientService delegate;

    public CustomJdbcOAuth2AuthorizedClientService(JdbcTemplate jdbcTemplate, ClientRegistrationRepository clientRegistrationRepository) {
        this.delegate = new JdbcOAuth2AuthorizedClientService(jdbcTemplate, clientRegistrationRepository);
    }

    @Override
    public void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient, Authentication principal) {
        OAuth2AuthorizedClient existingClient = loadAuthorizedClient(authorizedClient.getClientRegistration().getRegistrationId(), principal.getName());

        VendorOAuth2AuthorizedClientHandler handler = getHandler(authorizedClient.getClientRegistration().getRegistrationId());

        OAuth2AuthorizedClient clientToSave = handler.handle(authorizedClient, existingClient);
        delegate.saveAuthorizedClient(clientToSave, principal);
    }

    @Override
    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String clientRegistrationId, String principalName) {
        return delegate.loadAuthorizedClient(clientRegistrationId, principalName);
    }

    @Override
    public void removeAuthorizedClient(String clientRegistrationId, String principalName) {
        delegate.removeAuthorizedClient(clientRegistrationId, principalName);
    }
}