package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CachedBodyHttpServletRequestTest {

    @Test
    void httpServletRequestBodyInputStreamGivesTheSameBody() throws IOException {
        byte[] cachedBody = "{\"hearingRequestID\" :2000000018,\"status\" : \"HEARING_REQUESTED\"}".getBytes();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContent(cachedBody);
        CachedBodyHttpServletRequest request = new CachedBodyHttpServletRequest(mockRequest);

        InputStream is = request.getInputStream();

        assertThat(is.readAllBytes()).isEqualTo(cachedBody);
    }

    @Test
    void httpServletRequestBodyGetReaderGivesTheSameBody() throws IOException {
        byte[] cachedBody = "{\"hearingRequestID\" :2000000018,\"status\" : \"HEARING_REQUESTED\"}".getBytes();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContent(cachedBody);
        CachedBodyHttpServletRequest request = new CachedBodyHttpServletRequest(mockRequest);

        BufferedReader reader = request.getReader();

        String line = "";
        StringBuilder sb = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        assertThat(new String(cachedBody)).isEqualTo(sb.toString());
    }

}
