package uk.gov.hmcts.reform.hmc.interceptors;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Filter to cache the request body, so it can be read multiple times.
 */
@Configuration
public class ContentBodyCachingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request, @NonNull HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        filterChain.doFilter(new ContentCachingRequestWrapper(request), response);
    }
}
