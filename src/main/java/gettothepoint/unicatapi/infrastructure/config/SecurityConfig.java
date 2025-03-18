package gettothepoint.unicatapi.infrastructure.config;

import gettothepoint.unicatapi.infrastructure.security.oauth2.client.CustomOAuth2AuthenticationSuccessHandler;
import gettothepoint.unicatapi.infrastructure.security.oauth2.client.CustomOAuth2UserService;
import gettothepoint.unicatapi.infrastructure.security.oauth2.client.authorizedclient.HttpCookieOAuth2AuthorizationRequestRepository;
import gettothepoint.unicatapi.infrastructure.security.oauth2.resourceserver.CustomAuthenticationEntryPoint;
import gettothepoint.unicatapi.infrastructure.security.oauth2.resourceserver.MultiBearerTokenResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2AuthenticationSuccessHandler oauth2SuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final MultiBearerTokenResolver multiBearerTokenResolver;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/error").permitAll()
                        .requestMatchers("/v3/api-docs", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/.well-known/**", "/auth/oauth-links").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/sign-in", "/auth/sign-up").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/sign-out").permitAll()
                        .requestMatchers("/verification/email", "/verification/email/resend").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/members/anonymous/password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/members/anonymous/password").permitAll()
                        .requestMatchers(HttpMethod.GET, "/project/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorizationEndpoint -> authorizationEndpoint
                                .authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository)
                        )
                        .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oauth2SuccessHandler)
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .bearerTokenResolver(multiBearerTokenResolver)
                        .jwt(jwtConfigurer -> jwtConfigurer
                                .jwtAuthenticationConverter(jwtAuthenticationConverter)
                        )
                );

        return http.build();
    }
}
