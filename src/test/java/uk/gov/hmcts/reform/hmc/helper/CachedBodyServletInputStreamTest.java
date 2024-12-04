package uk.gov.hmcts.reform.hmc.helper;


import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.servlet.ReadListener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class CachedBodyServletInputStreamTest {

    private CachedBodyServletInputStream servletInputStream;

    @Test
    void servletInputStreamCreatedReturnsFalseOnFinished() {
        byte[] cachedBody = "{\"hearingRequestID\" :2000000018,\"status\" : \"HEARING_REQUESTED\"}".getBytes();
        servletInputStream = new CachedBodyServletInputStream(cachedBody);

        assertThat(servletInputStream.isFinished()).isFalse();
    }

    @Test
    void servletInputStreamCreatedReturnsTrueOnBodyRead() throws IOException {
        byte[] cachedBody = "{\"hearingRequestID\" :2000000018,\"status\" : \"HEARING_REQUESTED\"}".getBytes();
        servletInputStream = new CachedBodyServletInputStream(cachedBody);
        IOUtils.toByteArray(servletInputStream);

        assertThat(servletInputStream.isFinished()).isTrue();
    }

    @Test
    void servletInputStreamCreatedAndBodyReadReturnsReadyTrue() {
        byte[] cachedBody = "{\"hearingRequestID\" :2000000018,\"status\" : \"HEARING_REQUESTED\"}".getBytes();
        servletInputStream = new CachedBodyServletInputStream(cachedBody);

        assertThat(servletInputStream.isReady()).isTrue();
    }

    @Test
    void servletInputStreamReturnsBody() throws IOException {
        byte[] cachedBody = "{\"hearingRequestID\" :2000000018,\"status\" : \"HEARING_REQUESTED\"}".getBytes();
        servletInputStream = new CachedBodyServletInputStream(cachedBody);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int len = 0;
        byte[] buffer = new byte[1024];
        while ((len = servletInputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }

        assertThat(new String(cachedBody)).isEqualTo(baos.toString());
    }

    @Test
    void servletInputStreamSettingReadListenerThrowsException() {

        byte[] cachedBody = "{\"hearingRequestID\" :2000000018,\"status\" : \"HEARING_REQUESTED\"}".getBytes();
        servletInputStream = new CachedBodyServletInputStream(cachedBody);

        assertThatExceptionOfType(UnsupportedOperationException.class)
            .isThrownBy(() -> servletInputStream.setReadListener(Mockito.mock(ReadListener.class)));
    }
}
