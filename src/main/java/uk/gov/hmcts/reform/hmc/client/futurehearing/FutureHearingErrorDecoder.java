package uk.gov.hmcts.reform.hmc.client.futurehearing;

import com.microsoft.applicationinsights.core.dependencies.google.common.io.CharStreams;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.hmc.exceptions.AuthenticationException;
import uk.gov.hmcts.reform.hmc.exceptions.ResourceNotFoundException;

import java.io.IOException;
import java.io.Reader;

public class FutureHearingErrorDecoder implements ErrorDecoder {
    private static final Logger LOG = LoggerFactory.getLogger(FutureHearingErrorDecoder.class);
    public static final String INVALID_REQUEST = "Missing or invalid request parameters";
    public static final String INVALID_SECRET = "Authentication error";
    public static final String SERVER_ERROR = "Server error";
    public static final String REQUEST_NOT_FOUND = "Hearing request could not be found";

    @Override
    public Exception decode(String methodKey, Response response) {

        String message = null;
        Reader reader = null;

        try {
            reader = response.body().asReader(Util.UTF_8);
            message = CharStreams.toString(reader);
            reader.close();
        } catch (IOException exception) {
            LOG.error(exception.getMessage());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                LOG.error(exception.getMessage());
            }
        }
        if (message != null) {
            LOG.error(message);
        }
        switch (response.status()) {
            case 400:
                return new AuthenticationException(INVALID_REQUEST);
            case 401:
                return new AuthenticationException(INVALID_SECRET);
            case 404:
                return new ResourceNotFoundException(REQUEST_NOT_FOUND);
            default:
                return new AuthenticationException(SERVER_ERROR);
        }
    }
}
