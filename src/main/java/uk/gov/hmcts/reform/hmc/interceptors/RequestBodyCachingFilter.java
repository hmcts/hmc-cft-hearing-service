package uk.gov.hmcts.reform.hmc.interceptors;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.hmcts.reform.hmc.helper.CachedBodyHttpServletRequest;

import java.io.IOException;

/**
 * Filter to cache the request body, so it can be read multiple times.
 */
@Order(value = Ordered.HIGHEST_PRECEDENCE)
@Component
public class RequestBodyCachingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        CachedBodyHttpServletRequest cachedBodyHttpServletRequest = new CachedBodyHttpServletRequest(request);
        filterChain.doFilter(cachedBodyHttpServletRequest, response);
    }
}
