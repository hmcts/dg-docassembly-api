package uk.gov.hmcts.reform.dg.docassembly.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import uk.gov.hmcts.reform.idam.client.IdamApi;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestSecurityConfiguration {

    private final ClientRegistration clientRegistration;


    // to avoid sonar issue load from config
    @Value("${auth.idam.client.dummy-client-secret}")
    private String clientSecret;

    public TestSecurityConfiguration() {
        this.clientRegistration = clientRegistration().build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return mock(JwtDecoder.class);
    }

    @Bean
    public IdamApi idamApi() {
        return mock(IdamApi.class);
    }

    @Bean
    ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(clientRegistration);
    }

    private ClientRegistration.Builder clientRegistration() {

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("end_session_endpoint", "https://jhipster.org/logout");

        return ClientRegistration.withRegistrationId("oidc")
                .redirectUri("{baseUrl}/{action}/oauth2/code/{registrationId}")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .scope("read:user")
                .authorizationUri("https://jhipster.org/login/oauth/authorize")
                .tokenUri("https://jhipster.org/login/oauth/access_token")
                .jwkSetUri("https://jhipster.org/oauth/jwk")
                .userInfoUri("https://api.jhipster.org/user")
                .providerConfigurationMetadata(metadata)
                .userNameAttributeName("id")
                .clientName("Client Name")
                .clientId("client-id")
                .clientSecret(clientSecret);
    }

    @Bean
    public OAuth2AuthorizedClientService authorizedClientService(
            ClientRegistrationRepository clientRegistrationRepository
    ) {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }

    @Bean
    public OAuth2AuthorizedClientRepository authorizedClientRepository(
            OAuth2AuthorizedClientService authorizedClientService
    ) {
        return new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(authorizedClientService);
    }

}
