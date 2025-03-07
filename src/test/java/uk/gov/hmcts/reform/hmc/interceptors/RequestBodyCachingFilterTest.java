package uk.gov.hmcts.reform.hmc.interceptors;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import uk.gov.hmcts.reform.hmc.helper.CachedBodyHttpServletRequest;

import java.io.IOException;

@ExtendWith(MockitoExtension.class)
class RequestBodyCachingFilterTest {

    @InjectMocks
    private RequestBodyCachingFilter requestBodyCachingFilter;

    @Test
    void requestBodyCachingFilterCreatesRequestWrapper() throws IOException, ServletException {
        MockHttpServletRequest mockedRequest = new MockHttpServletRequest();
        MockHttpServletResponse mockedResponse = new MockHttpServletResponse();
        FilterChain mockedFilterChain = Mockito.mock(FilterChain.class);

        requestBodyCachingFilter.doFilter(mockedRequest, mockedResponse, mockedFilterChain);

        Mockito.verify(mockedFilterChain, Mockito.times(1)).doFilter(
            Mockito.any(CachedBodyHttpServletRequest.class), Mockito.eq(mockedResponse));
    }
}
