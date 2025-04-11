package com.project.foradhd.global.config;

import static org.springframework.http.HttpMethod.*;
import static org.springframework.security.config.Customizer.withDefaults;

import com.project.foradhd.domain.auth.converter.CustomOAuth2AuthorizationCodeGrantRequestEntityConverter;
import com.project.foradhd.domain.auth.filter.JwtAuthenticationFilter;
import com.project.foradhd.domain.auth.filter.LoginAuthenticationFilter;
import com.project.foradhd.domain.auth.handler.JwtAuthenticationFailureHandler;
import com.project.foradhd.domain.auth.handler.JwtAuthorizationFailureHandler;
import com.project.foradhd.domain.auth.handler.JwtLogoutSuccessHandler;
import com.project.foradhd.domain.auth.handler.LoginAuthenticationFailureHandler;
import com.project.foradhd.domain.auth.handler.LoginAuthenticationSuccessHandler;
import com.project.foradhd.domain.auth.handler.OAuth2AuthenticationFailureHandler;
import com.project.foradhd.domain.auth.handler.OAuth2AuthenticationSuccessHandler;
import com.project.foradhd.domain.user.persistence.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private static final String NICKNAME_CHECK_API_PATH = "/api/v1/user/nickname-check";
    private static final String EMAIL_CHECK_API_PATH = "/api/v1/user/email-check";
    private static final String EMAIL_AUTH_API_PATH = "/api/v1/user/email-auth";
    private static final String SIGN_UP_API_PATH = "/api/v1/user/sign-up";
    private static final String LOGIN_API_PATH = "/api/v1/auth/login";
    private static final String AUTH_TOKEN_REISSUE_API_PATH = "/api/v1/auth/reissue";
    private static final String S3_IMAGE_API_PATH = "/api/v1/files/images";
    private static final String HEALTH_CHECK_API_PATH = "/api/v1/health-check";

    private static final String SNS_SIGN_UP_API_PATH = "/api/v1/user/sns-sign-up";
    private static final String WITHDRAW_API_PATH = "/api/v1/user/withdraw";
    private static final String LOGOUT_API_PATH = "/api/v1/auth/logout";

    private static final String PROMETHEUS_PATH = "/actuator/prometheus";

    private static final RequestMatcher loginMatcher = new AntPathRequestMatcher(LOGIN_API_PATH, POST.name());
    private static final RequestMatcher logoutMatcher = new AntPathRequestMatcher(LOGOUT_API_PATH, DELETE.name());

    private final LoginAuthenticationSuccessHandler loginAuthenticationSuccessHandler;
    private final LoginAuthenticationFailureHandler loginAuthenticationFailureHandler;
    private final JwtLogoutSuccessHandler jwtLogoutSuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationFailureHandler jwtAuthenticationFailureHandler;
    private final JwtAuthorizationFailureHandler jwtAuthorizationFailureHandler;
    private final AuthenticationConfiguration authenticationConfiguration;

    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository;
    private final CustomOAuth2AuthorizationCodeGrantRequestEntityConverter customOAuth2AuthorizationCodeGrantRequestEntityConverter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(registry -> registry
                .requestMatchers(NICKNAME_CHECK_API_PATH, EMAIL_CHECK_API_PATH, EMAIL_AUTH_API_PATH,
                        SIGN_UP_API_PATH, LOGIN_API_PATH, AUTH_TOKEN_REISSUE_API_PATH, S3_IMAGE_API_PATH,
                        HEALTH_CHECK_API_PATH, PROMETHEUS_PATH).permitAll()
                .requestMatchers("/error", "/favicon.ico").permitAll()
                    .requestMatchers("/api/v1/notifications/sse").permitAll()
                .requestMatchers(SNS_SIGN_UP_API_PATH, WITHDRAW_API_PATH, LOGOUT_API_PATH).hasRole(Role.GUEST.name())
                .anyRequest().hasRole(Role.USER.name()))
            .sessionManagement(config -> config
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            //if Spring MVC is on classpath and no CorsConfigurationSource is provided,
            //Spring Security will use CORS configuration provided to Spring MVC
            .cors(withDefaults())
            .csrf(CsrfConfigurer::disable)
            .httpBasic(HttpBasicConfigurer::disable)
            .formLogin(FormLoginConfigurer::disable)
            .oauth2Login(config -> config
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler(oAuth2AuthenticationFailureHandler)
                .authorizationEndpoint(endPointConfig -> endPointConfig
                    .authorizationRequestRepository(authorizationRequestRepository))
                .tokenEndpoint(endPointConfig -> endPointConfig
                    .accessTokenResponseClient(accessTokenResponseClient()))
                .userInfoEndpoint(endPointConfig -> endPointConfig
                    .userService(oAuth2UserService)))
            .logout(config -> config
                .logoutRequestMatcher(logoutMatcher)
                .clearAuthentication(true)
                .logoutSuccessHandler(jwtLogoutSuccessHandler))
            .exceptionHandling(config -> config
                .authenticationEntryPoint(jwtAuthenticationFailureHandler)
                .accessDeniedHandler(jwtAuthorizationFailureHandler))
            .addFilter(loginAuthenticationFilter(authenticationManager(authenticationConfiguration)))
            .addFilterAfter(jwtAuthenticationFilter, LoginAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
        throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public LoginAuthenticationFilter loginAuthenticationFilter(AuthenticationManager authenticationManager) {
        LoginAuthenticationFilter loginAuthenticationFilter = new LoginAuthenticationFilter();
        loginAuthenticationFilter.setRequiresAuthenticationRequestMatcher(loginMatcher);
        loginAuthenticationFilter.setAuthenticationManager(authenticationManager);
        loginAuthenticationFilter.setAuthenticationSuccessHandler(loginAuthenticationSuccessHandler);
        loginAuthenticationFilter.setAuthenticationFailureHandler(loginAuthenticationFailureHandler);
        return loginAuthenticationFilter;
    }

    //LoginAuthenticationFilter 서블릿 컨테이너(ApplicationFilterChain)에 등록되지 않도록 설정
    @Bean
    public FilterRegistrationBean<LoginAuthenticationFilter> loginAuthenticationFilterRegistrationBean(AuthenticationManager authenticationManager) {
        FilterRegistrationBean<LoginAuthenticationFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(loginAuthenticationFilter(authenticationManager));
        filterRegistrationBean.setEnabled(false);
        return filterRegistrationBean;
    }

    //JwtAuthenticationFilter 서블릿 컨테이너(ApplicationFilterChain)에 등록되지 않도록 설정
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistrationBean() {
        FilterRegistrationBean<JwtAuthenticationFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(jwtAuthenticationFilter);
        filterRegistrationBean.setEnabled(false);
        return filterRegistrationBean;
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy(Role.hierarchy());
        return roleHierarchy;
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler methodSecurityExpressionHandler = new DefaultMethodSecurityExpressionHandler();
        methodSecurityExpressionHandler.setRoleHierarchy(roleHierarchy);
        return methodSecurityExpressionHandler;
    }

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient oAuth2AccessTokenResponseClient = new DefaultAuthorizationCodeTokenResponseClient();
        oAuth2AccessTokenResponseClient.setRequestEntityConverter(customOAuth2AuthorizationCodeGrantRequestEntityConverter);
        return oAuth2AccessTokenResponseClient;
    }
}
