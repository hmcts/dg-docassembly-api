package uk.gov.hmcts.reform.dg.docassembly.config.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;

import java.util.List;
import java.util.Set;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Profile({"!integration-web-test"})
public class SecurityConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfiguration.class);

    @Value("${spring.security.oauth2.client.provider.oidc.issuer-uri}")
    private String issuerUri;

    @Value("${oidc.issuer}")
    private String issuerOverride;

    private final ServiceAuthFilter serviceAuthFilter;

    public SecurityConfiguration(final ServiceAuthFilter serviceAuthFilter) {
        this.serviceAuthFilter = serviceAuthFilter;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers("/swagger-ui.html",
                "/swagger-ui/**",
                "/swagger-resources/**",
                "/v3/**",
                "/health",
                "/health/liveness",
                "/health/readiness",
                "/status/health",
                "/loggers/**",
                "/");
    }

    @Bean
    protected SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .addFilterBefore(serviceAuthFilter, BearerTokenAuthenticationFilter.class)
            .sessionManagement(sessionManagementConfigurer ->
                sessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry ->
                authorizationManagerRequestMatcherRegistry.requestMatchers("/api/**").authenticated())
            .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry ->
                authorizationManagerRequestMatcherRegistry.requestMatchers("/error").authenticated())
            .oauth2ResourceServer(httpSecurityOAuth2ResourceServerConfigurer ->
                    httpSecurityOAuth2ResourceServerConfigurer.jwt(Customizer.withDefaults()))
            .oauth2Client(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder(IdamSecurityProperties securityProperties) {
        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromOidcIssuerLocation(issuerUri);
        OAuth2TokenValidator<Jwt> withTimestamp = new JwtTimestampValidator();
        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
            withTimestamp,
            allowedIssuersValidator(securityProperties.getAllowedIssuers())
        ));
        return jwtDecoder;
    }

    OAuth2TokenValidator<Jwt> allowedIssuersValidator(List<String> allowedIssuers) {
        Set<String> allowedIssuerSet = Set.copyOf(allowedIssuers);

        return new JwtClaimValidator<>("iss", iss -> isAllowedIssuer(iss, allowedIssuerSet));
    }

    private boolean isAllowedIssuer(Object iss, Set<String> allowedIssuerSet) {
        if (iss instanceof String issuer && allowedIssuerSet.contains(issuer)) {
            return true;
        }
        log.warn("JWT rejected: issuer not allowed");
        return false;
    }

}
