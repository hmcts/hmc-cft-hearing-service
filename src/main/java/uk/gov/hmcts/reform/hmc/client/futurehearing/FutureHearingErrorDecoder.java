package uk.gov.hmcts.reform.hmc.client.futurehearing;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.hmc.client.hmi.ErrorDetails;
import uk.gov.hmcts.reform.hmc.exceptions.BadFutureHearingRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.FutureHearingServerException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class FutureHearingErrorDecoder implements ErrorDecoder {
    public static final String INVALID_REQUEST = "Missing or invalid request parameters";
    public static final String INVALID_SECRET = "Authentication error";
    public static final String SERVER_ERROR = "Server error";

    @Override
    public Exception decode(String methodKey, Response response) {
        ErrorDetails errorDetails = getResponseBody(response, ErrorDetails.class)
            .orElseThrow(() -> new BadFutureHearingRequestException(INVALID_REQUEST));
        log.error(String.format("Response from FH failed with error code %s, error message '%s'",
                                errorDetails.getErrorCode(),
                                errorDetails.getErrorDescription()));

        if (String.valueOf(response.status()).startsWith("4")) {
            return new BadFutureHearingRequestException(INVALID_REQUEST);
        } else {
            return new FutureHearingServerException(SERVER_ERROR);
        }
    }

    private <T> Optional<T> getResponseBody(Response response, Class<T> klass) {
        try {
            String bodyJson = new BufferedReader(new InputStreamReader(response.body().asInputStream()))
                .lines().parallel().collect(Collectors.joining("\n"));
            return Optional.ofNullable(new ObjectMapper().readValue(bodyJson, klass));
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
