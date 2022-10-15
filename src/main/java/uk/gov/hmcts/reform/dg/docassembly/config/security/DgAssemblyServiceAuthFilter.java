package uk.gov.hmcts.reform.dg.docassembly.config.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.authorisation.exceptions.ServiceException;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;
import uk.gov.hmcts.reform.authorisation.validators.ServiceAuthTokenValidator;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@ConditionalOnProperty("idam.s2s-custom-authorised.services")
public class DgAssemblyServiceAuthFilter extends OncePerRequestFilter {

    public static final String AUTHORISATION = "ServiceAuthorization";

    private static final Logger LOG = LoggerFactory.getLogger(DgAssemblyServiceAuthFilter.class);

    private final List<String> authorisedServices;

    private final AuthTokenValidator authTokenValidator;


    public DgAssemblyServiceAuthFilter(
            ServiceAuthorisationApi authorisationApi,
            @Value("${idam.s2s-custom-authorised.services}") List<String> authorisedServices
    ) {
        this.authTokenValidator = new ServiceAuthTokenValidator(authorisationApi);
        if (authorisedServices == null || authorisedServices.isEmpty()) {
            throw new IllegalArgumentException("Must have at least one service defined");
        }
        this.authorisedServices = authorisedServices.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String bearerToken = extractBearerToken(request);
            String serviceName = this.authTokenValidator.getServiceName(bearerToken);
            LOG.info("dg-docassembly : Endpoint : {}  for : {} method is accessed by {} ", request.getRequestURI(),
                    request.getMethod(), serviceName);
            if (!authorisedServices.contains(serviceName)) {
                LOG.debug("service forbidden {}", serviceName);
                response.setStatus(HttpStatus.FORBIDDEN.value());
            } else {
                LOG.debug("service authorized {}", serviceName);
                filterChain.doFilter(request, response);
            }
        } catch (InvalidTokenException | ServiceException exception) {
            LOG.warn("Unsuccessful service authentication", exception);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        }
    }

    private String extractBearerToken(HttpServletRequest request) {
        String token = request.getHeader(AUTHORISATION);
        if (token == null) {
            throw new InvalidTokenException("ServiceAuthorization Token is missing");
        }
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }
}
