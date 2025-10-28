package com.eventure.calendar_app.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.eventure.calendar_app.user.service.CustomOAuth2UserService;

import jakarta.servlet.Filter;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private jwtFilter jwtFilter;

    @Mock
    private CustomOAuth2UserService customOAuth2UserService;

    @Mock
    private OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    @Mock
    private AuthenticationManager authenticationManager;

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig();
        ReflectionTestUtils.setField(securityConfig, "userDetailsService", userDetailsService);
        ReflectionTestUtils.setField(securityConfig, "jwtFilter", jwtFilter);
        ReflectionTestUtils.setField(securityConfig, "customOAuth2UserService", customOAuth2UserService);
        ReflectionTestUtils.setField(securityConfig, "oauth2LoginSuccessHandler", oauth2LoginSuccessHandler);
    }

    @Test
    void testAuthProvider() {
        AuthenticationProvider provider = securityConfig.authProvider();

        assertThat(provider).isInstanceOf(DaoAuthenticationProvider.class);
        DaoAuthenticationProvider daoProvider = (DaoAuthenticationProvider) provider;
        UserDetailsService configuredUserDetailsService =
                (UserDetailsService) ReflectionTestUtils.getField(daoProvider, "userDetailsService");
        assertThat(configuredUserDetailsService).isEqualTo(userDetailsService);

        PasswordEncoder passwordEncoder = (PasswordEncoder)
                Objects.requireNonNull(ReflectionTestUtils.invokeMethod(daoProvider, "getPasswordEncoder"));

        assertThat(passwordEncoder).isInstanceOf(BCryptPasswordEncoder.class);
        String rawPassword = "password";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
    }

    @Test
    void testAuthenticationManager() throws Exception {
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);

        AuthenticationManager result = securityConfig.authenticationManager(authenticationConfiguration);

        assertThat(result).isSameAs(authenticationManager);
    }

    @Test
    void testCorsConfigurationSource() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();

        assertThat(source).isInstanceOf(UrlBasedCorsConfigurationSource.class);

        CorsConfiguration configuration = Objects.requireNonNull(
                source.getCorsConfiguration(new MockHttpServletRequest()));
        assertThat(configuration.getAllowedOrigins())
                .containsExactlyInAnyOrder("http://127.0.0.1:5173", "http://localhost:5173");
        assertThat(configuration.getAllowedMethods())
                .containsExactlyInAnyOrder("GET", "POST", "PUT", "DELETE", "OPTIONS");
        assertThat(configuration.getAllowedHeaders())
                .containsExactlyInAnyOrder("Authorization", "Content-Type", "X-Requested-With");
        assertThat(configuration.getExposedHeaders())
                .containsExactly("Authorization");
        assertThat(configuration.getAllowCredentials()).isTrue();
    }

    @Test
    void testSecurityFilterChain() throws Exception {
        SecurityFilterChain securityFilterChain = buildSecurityFilterChain();

        assertThat(securityFilterChain).isNotNull();

        List<Filter> filters = securityFilterChain.getFilters();
        assertThat(filters).contains(jwtFilter);

        int jwtFilterIndex = filters.indexOf(jwtFilter);
        int usernameFilterIndex = IntStream.range(0, filters.size())
                .filter(i -> filters.get(i) instanceof UsernamePasswordAuthenticationFilter)
                .findFirst()
                .orElse(-1);

        if (usernameFilterIndex >= 0) {
            assertThat(jwtFilterIndex).isLessThan(usernameFilterIndex);
        }
    }

    private SecurityFilterChain buildSecurityFilterChain() throws Exception {
        ObjectPostProcessor<Object> objectPostProcessor = new ObjectPostProcessor<>() {
            @Override
            public <O> O postProcess(O object) {
                return object;
            }
        };

        AuthenticationManagerBuilder authenticationManagerBuilder = new AuthenticationManagerBuilder(objectPostProcessor);
        authenticationManagerBuilder.parentAuthenticationManager(authenticationManager);

        Map<Class<?>, Object> sharedObjects = new HashMap<>();
        sharedObjects.put(AuthenticationManager.class, authenticationManager);
        sharedObjects.put(ClientRegistrationRepository.class, mock(ClientRegistrationRepository.class));
        sharedObjects.put(OAuth2AuthorizedClientService.class, mock(OAuth2AuthorizedClientService.class));
        sharedObjects.put(OAuth2AuthorizedClientRepository.class, mock(OAuth2AuthorizedClientRepository.class));
        sharedObjects.put(CorsConfigurationSource.class, securityConfig.corsConfigurationSource());
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.getBeanFactory()
                .registerSingleton("corsConfigurationSource", securityConfig.corsConfigurationSource());
        applicationContext.refresh();
        sharedObjects.put(ApplicationContext.class, applicationContext);

        HttpSecurity httpSecurity = new HttpSecurity(objectPostProcessor, authenticationManagerBuilder, sharedObjects);
        return securityConfig.securityFilterChain(httpSecurity);
    }
}
