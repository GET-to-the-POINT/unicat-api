package taeniverse.unicatApi.mvc.model.dto;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public record PrincipalDetails(OAuthDTO user) implements OAuth2User, UserDetails {


    @Override
    public String getName() {
        return user.getName();
    }


    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> authorities = new ArrayList<>();

        authorities.add((GrantedAuthority) user::getRole);

        return authorities;
    }

    public String getUsername() {
        return user.getUsername();
    }


    public Long getUserId() {
        return user.getUserId();
    }

    @Override
    public String getPassword() {
        return "";
    }

}
