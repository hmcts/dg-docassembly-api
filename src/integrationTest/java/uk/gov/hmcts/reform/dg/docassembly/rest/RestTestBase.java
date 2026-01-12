package uk.gov.hmcts.reform.dg.docassembly.rest;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.dg.docassembly.Application;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@ActiveProfiles("integration-web-test")
@SpringBootTest(classes = {Application.class, TestSecurityConfiguration.class})
public abstract class RestTestBase {

    private final WebApplicationContext context;

    private static final String DUMMY_ID_JWT = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9"
            + ".eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsIm"
            + "p0aSI6ImQzNWRmMTRkLTA5ZjYtNDhmZi04YTkzLTdjNmYwMzM5MzE1OSIsImlhdCI6MTU0M"
            + "Tk3MTU4MywiZXhwIjoxNTQxOTc1MTgzfQ.QaQOarmV8xEUYV7yvWzX3cUE_4W1luMcWCwpr"
            + "oqqUrg";

    protected MockMvc restLogoutMockMvc;

    @MockitoBean
    private IdamClient idamClient;

    RestTestBase(WebApplicationContext context) {
        this.context = context;
    }

    @BeforeEach
    void setupMocks() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("groups", "ROLE_USER");
        claims.put("sub", 123);

        OidcIdToken idToken = new OidcIdToken(DUMMY_ID_JWT, Instant.now(),
                Instant.now().plusSeconds(60), claims);

        SecurityContextHolder.getContext().setAuthentication(authenticationToken(idToken));
        this.restLogoutMockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
    }

    private OAuth2AuthenticationToken authenticationToken(OidcIdToken idToken) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("USER"));
        OidcUser user = new DefaultOidcUser(authorities, idToken);

        return new OAuth2AuthenticationToken(user, authorities, "oidc");
    }
}
