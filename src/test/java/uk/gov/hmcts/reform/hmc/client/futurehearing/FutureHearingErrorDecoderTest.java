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
import uk.gov.hmcts.reform.hmc.exceptions.BadFutureHearingRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.FutureHearingServerException;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.hmc.client.futurehearing.FutureHearingErrorDecoder.INVALID_REQUEST;
import static uk.gov.hmcts.reform.hmc.client.futurehearing.FutureHearingErrorDecoder.SERVER_ERROR;

class FutureHearingErrorDecoderTest {

    private String methodKey = null;
    private Response response;
    private byte[] byteArray;
    private static final String INPUT_STRING = "{\"statusCode\":\"400\",\"message\":\"Resource not found\"}";
    private RequestTemplate template;

    @InjectMocks
    private FutureHearingErrorDecoder futureHearingErrorDecoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        byteArray = INPUT_STRING.getBytes();
    }

    @Test
    void shouldThrowBadFutureHearingRequestExceptionWith400Error() {

        Logger logger = (Logger) LoggerFactory.getLogger(FutureHearingErrorDecoder.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        response = Response.builder()
            .body(byteArray)
            .status(400)
            .request(Request.create(HttpMethod.POST, "/api", Collections.emptyMap(), null, Util.UTF_8, template))
            .build();

        Exception exception = futureHearingErrorDecoder.decode(methodKey, response);

        assertThat(exception).isInstanceOf(BadFutureHearingRequestException.class);
        assertEquals(INVALID_REQUEST, exception.getMessage());
        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(1, logsList.size());

        ILoggingEvent loggingEvent = logsList.getFirst();
        assertEquals(Level.ERROR, loggingEvent.getLevel());
        assertEquals("Response from FH failed with error code 400, "
            + "error message " + INPUT_STRING, loggingEvent.getFormattedMessage());

        logger.detachAndStopAllAppenders();
    }

    @Test
    void shouldThrowBadFutureHearingRequestExceptionWith401Error() {

        Logger logger = (Logger) LoggerFactory.getLogger(FutureHearingErrorDecoder.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        response = Response.builder()
            .body(byteArray)
            .status(401)
            .request(Request.create(HttpMethod.POST, "/api", Collections.emptyMap(), null, Util.UTF_8, template))
            .build();

        Exception exception = futureHearingErrorDecoder.decode(methodKey, response);

        assertThat(exception).isInstanceOf(BadFutureHearingRequestException.class);
        assertEquals(INVALID_REQUEST, exception.getMessage());
        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(1, logsList.size());

        ILoggingEvent loggingEvent = logsList.getFirst();
        assertEquals(Level.ERROR, loggingEvent.getLevel());
        assertEquals("Response from FH failed with error code 401, "
            + "error message " + INPUT_STRING, loggingEvent.getFormattedMessage());

        logger.detachAndStopAllAppenders();
    }

    @Test
    void shouldThrowBadFutureHearingRequestExceptionWith404Error() {

        Logger logger = (Logger) LoggerFactory.getLogger(FutureHearingErrorDecoder.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        response = Response.builder()
            .body(byteArray)
            .status(404)
            .request(Request.create(HttpMethod.PUT, "/api", Collections.emptyMap(), null, Util.UTF_8, template))
            .build();

        Exception exception = futureHearingErrorDecoder.decode(methodKey, response);

        assertThat(exception).isInstanceOf(BadFutureHearingRequestException.class);
        assertEquals(INVALID_REQUEST, exception.getMessage());
        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(1, logsList.size());

        ILoggingEvent loggingEvent = logsList.getFirst();
        assertEquals(Level.ERROR, loggingEvent.getLevel());
        assertEquals("Response from FH failed with error code 404, "
            + "error message " + INPUT_STRING, loggingEvent.getFormattedMessage());

        logger.detachAndStopAllAppenders();
    }

    @Test
    void shouldThrowAuthenticationExceptionWith500Error() {

        Logger logger = (Logger) LoggerFactory.getLogger(FutureHearingErrorDecoder.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        response = Response.builder()
            .body(byteArray)
            .status(500)
            .request(Request.create(HttpMethod.POST, "/api", Collections.emptyMap(), null, Util.UTF_8, template))
            .build();

        Exception exception = futureHearingErrorDecoder.decode(methodKey, response);

        assertThat(exception).isInstanceOf(FutureHearingServerException.class);
        assertEquals(SERVER_ERROR, exception.getMessage());
        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(1, logsList.size());

        ILoggingEvent loggingEvent = logsList.getFirst();
        assertEquals(Level.ERROR, loggingEvent.getLevel());
        assertEquals("Response from FH failed with error code 500, "
                         + "error message " + INPUT_STRING, loggingEvent.getFormattedMessage());

        logger.detachAndStopAllAppenders();
    }
}
