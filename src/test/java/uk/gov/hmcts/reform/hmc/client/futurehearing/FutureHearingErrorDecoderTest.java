package uk.gov.hmcts.reform.hmc.client.futurehearing;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import feign.Request;
import feign.Request.HttpMethod;
import feign.RequestTemplate;
import feign.Response;
import feign.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.hmc.exceptions.AuthenticationException;
import uk.gov.hmcts.reform.hmc.exceptions.ResourceNotFoundException;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.hmc.client.futurehearing.FutureHearingErrorDecoder.INVALID_REQUEST;
import static uk.gov.hmcts.reform.hmc.client.futurehearing.FutureHearingErrorDecoder.INVALID_SECRET;
import static uk.gov.hmcts.reform.hmc.client.futurehearing.FutureHearingErrorDecoder.REQUEST_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.client.futurehearing.FutureHearingErrorDecoder.SERVER_ERROR;

class FutureHearingErrorDecoderTest {

    private String methodKey = null;
    private Response response;
    private byte[] byteArrray;
    private String inputString = "This response message should be logged";
    private RequestTemplate template;

    @InjectMocks
    private FutureHearingErrorDecoder futureHearingErrorDecoder;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        byteArrray = inputString.getBytes();
    }

    @Test
    void shouldThrowAuthenticationExceptionWith400Error() {

        Logger logger = (Logger) LoggerFactory.getLogger(FutureHearingErrorDecoder.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        response = Response.builder()
            .body(byteArrray)
            .status(400)
            .request(Request.create(HttpMethod.POST, "/api", Collections.emptyMap(), null, Util.UTF_8, template))
            .build();

        Exception exception = futureHearingErrorDecoder.decode(methodKey, response);

        assertThat(exception).isInstanceOf(AuthenticationException.class);
        assertEquals(INVALID_REQUEST, exception.getMessage());
        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(1, logsList.size());
        assertEquals(Level.ERROR, logsList.get(0)
            .getLevel());
        assertEquals(inputString, logsList.get(0)
            .getMessage());
    }

    @Test
    void shouldThrowAuthenticationExceptionWith401Error() {

        Logger logger = (Logger) LoggerFactory.getLogger(FutureHearingErrorDecoder.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        response = Response.builder()
            .body(byteArrray)
            .status(401)
            .request(Request.create(HttpMethod.POST, "/api", Collections.emptyMap(), null, Util.UTF_8, template))
            .build();

        Exception exception = futureHearingErrorDecoder.decode(methodKey, response);

        assertThat(exception).isInstanceOf(AuthenticationException.class);
        assertEquals(INVALID_SECRET, exception.getMessage());
        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(1, logsList.size());
        assertEquals(Level.ERROR, logsList.get(0)
            .getLevel());
        assertEquals(inputString, logsList.get(0)
            .getMessage());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWith404Error() {

        Logger logger = (Logger) LoggerFactory.getLogger(FutureHearingErrorDecoder.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        response = Response.builder()
            .body(byteArrray)
            .status(404)
            .request(Request.create(HttpMethod.PUT, "/api", Collections.emptyMap(), null, Util.UTF_8, template))
            .build();

        Exception exception = futureHearingErrorDecoder.decode(methodKey, response);

        assertThat(exception).isInstanceOf(ResourceNotFoundException.class);
        assertEquals(REQUEST_NOT_FOUND, exception.getMessage());
        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(1, logsList.size());
        assertEquals(Level.ERROR, logsList.get(0)
            .getLevel());
        assertEquals(inputString, logsList.get(0)
            .getMessage());
    }

    @Test
    void shouldThrowAuthenticationExceptionWith500Error() {

        Logger logger = (Logger) LoggerFactory.getLogger(FutureHearingErrorDecoder.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        response = Response.builder()
            .body(byteArrray)
            .status(500)
            .request(Request.create(HttpMethod.POST, "/api", Collections.emptyMap(), null, Util.UTF_8, template))
            .build();

        Exception exception = futureHearingErrorDecoder.decode(methodKey, response);

        assertThat(exception).isInstanceOf(AuthenticationException.class);
        assertEquals(SERVER_ERROR, exception.getMessage());
        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(1, logsList.size());
        assertEquals(Level.ERROR, logsList.get(0)
            .getLevel());
        assertEquals(inputString, logsList.get(0)
            .getMessage());
    }
}
