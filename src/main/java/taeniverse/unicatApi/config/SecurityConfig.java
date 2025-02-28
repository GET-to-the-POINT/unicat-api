package taeniverse.unicatApi.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import taeniverse.unicatApi.mvc.service.CustomOAuth2UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2AuthenticationSuccessHandler oauth2SuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final ClientRegistrationRepository clientRegistrationRepository;

    public SecurityConfig(CustomOAuth2AuthenticationSuccessHandler oauth2SuccessHandler,
                            CustomOAuth2UserService customOAuth2UserService,
                            ClientRegistrationRepository clientRegistrationRepository
    ) {
        this.oauth2SuccessHandler = oauth2SuccessHandler;
        this.customOAuth2UserService = customOAuth2UserService;
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//                .sessionManagement(session ->
//                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/sign-in", "/api/sign-up").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorizationEndpoint ->
                                authorizationEndpoint.authorizationRequestResolver(new OAuth2AuthorizationRequestResolver() {
                                    // DefaultOAuth2AuthorizationRequestResolver를 내부에서 생성
                                    private final DefaultOAuth2AuthorizationRequestResolver defaultResolver =
                                            new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");

                                    @Override
                                    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                                        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
                                        return customize(request, authorizationRequest);
                                    }

                                    @Override
                                    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
                                        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request, clientRegistrationId);
                                        return customize(request, authorizationRequest);
                                    }

                                    // 추가 파라미터를 설정하는 메서드
                                    private OAuth2AuthorizationRequest customize(HttpServletRequest request, OAuth2AuthorizationRequest authorizationRequest) {
                                        // 요청 URI에서 registrationId 추출 (예: "/oauth2/authorization/google" -> "google")
                                        String requestUri = request.getRequestURI();
                                        String baseUri = "/oauth2/authorization/";
                                        String registrationId = null;
                                        if (requestUri.startsWith(baseUri)) {
                                            registrationId = requestUri.substring(baseUri.length());
                                        }
                                        if (authorizationRequest != null && "google".equals(registrationId)) {
                                            Map<String, Object> additionalParameters = new HashMap<>(authorizationRequest.getAdditionalParameters());
                                            additionalParameters.put("access_type", "offline");
                                            additionalParameters.put("prompt", "consent");
                                            return OAuth2AuthorizationRequest.from(authorizationRequest)
                                                    .additionalParameters(additionalParameters)
                                                    .build();
                                        }
                                        return authorizationRequest;
                                    }
                                })
                        )
                        .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oauth2SuccessHandler)
                )
//                .exceptionHandling(exception -> exception
//                        .authenticationEntryPoint((request, response, authException) -> {
//                            response.sendError(HttpServletResponse.SC_FORBIDDEN); // 인증이 필요한 요청에서 인증 정보가 없을 경우 403 반환
//                        })
//                )
        ;
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("https://unicat.day", "https://api.unicat.day"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}