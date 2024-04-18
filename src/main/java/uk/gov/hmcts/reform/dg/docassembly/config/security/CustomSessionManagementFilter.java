package uk.gov.hmcts.reform.dg.docassembly.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.session.SessionManagementFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomSessionManagementFilter extends SessionManagementFilter {

    public CustomSessionManagementFilter(HttpSessionSecurityContextRepository securityContextRepository) {
        super(securityContextRepository);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        super.doFilter(request, response, chain);
    }
}
