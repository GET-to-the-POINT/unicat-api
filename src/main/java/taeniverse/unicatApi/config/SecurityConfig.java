package taeniverse.unicatApi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import taeniverse.unicatApi.OAuth2.CustomSuccessHandler;
import taeniverse.unicatApi.mvc.service.CustomOAuth2UserService;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Main Security Configuration for the application.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Value("${security.headers.frame-options:DENY}")
    private String frameOptions;

    public SecurityConfig(
            AuthenticationConfiguration authenticationConfiguration,
            JWTUtil jwtUtil,
            CustomOAuth2UserService customOAuth2UserService,
            CustomSuccessHandler customSuccessHandler,
            ClientRegistrationRepository clientRegistrationRepository
    ) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtUtil = jwtUtil;
        this.customOAuth2UserService = customOAuth2UserService;
        this.customSuccessHandler = customSuccessHandler;
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Spring Security에서 사용할 AuthenticationManager를 빈으로 등록.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * Spring Security FilterChain 설정.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        disableUnnecessaryFeatures(http);
        setupCors(http);
        setupOAuth2Login(http);
        configureAuthorizationRules(http);
        registerCustomFilters(http);
        configureSessionManagement(http);
        configureSecurityHeaders(http);

        return http.build();
    }

    /**
     * CSRF, FormLogin, HttpBasic 등 사용하지 않는 기능 비활성화
     */
    private void disableUnnecessaryFeatures(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);
    }

    /**
     * CORS 설정
     */
    private void setupCors(HttpSecurity http) throws Exception {
        http.cors(withDefaults());
    }

    /**
     * OAuth2 로그인 과정에서 Custom Resolver와 SuccessHandler를 사용하도록 설정
     */
    private void setupOAuth2Login(HttpSecurity http) throws Exception {
        http.oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(endpoint -> endpoint
                        .authorizationRequestResolver(
                                new CustomAuthorizationRequestResolver(clientRegistrationRepository)))
                .userInfoEndpoint(userInfo -> userInfo
                        .userService(customOAuth2UserService))
                .successHandler(customSuccessHandler)
        );
    }

    /**
     * URL별 권한 부여 설정
     */
    private void configureAuthorizationRules(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/login", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/sign-in", "/api/sign-up").permitAll()
                .requestMatchers("/api/user").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/oauth2").permitAll()
                .anyRequest().authenticated()
        );
    }

    /**
     * Custom JWTFilter와 SecurityFilter 등록
     */
    private void registerCustomFilters(HttpSecurity http) throws Exception {
        http.addFilterAfter(new JWTFilter(jwtUtil), OAuth2LoginAuthenticationFilter.class)
                .addFilterAt(new SecurityFilter(authenticationManager(authenticationConfiguration)),
                        UsernamePasswordAuthenticationFilter.class);
    }

    /**
     * 세션 사용 전략 - STATELESS
     */
    private void configureSessionManagement(HttpSecurity http) throws Exception {
        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );
    }

    /**
     * Security 헤더 설정 (X-Frame-Options을 프로퍼티 기반으로 적용)
     */
    private void configureSecurityHeaders(HttpSecurity http) throws Exception {
        if ("DISABLE".equalsIgnoreCase(frameOptions)) {
            http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));
        }
    }
}
