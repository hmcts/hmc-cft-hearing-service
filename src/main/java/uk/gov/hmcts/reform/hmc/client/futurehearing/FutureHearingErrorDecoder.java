package uk.gov.hmcts.reform.hmc.client.futurehearing;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.hmc.exceptions.BadFutureHearingRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.FutureHearingServerException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Slf4j
public class FutureHearingErrorDecoder implements ErrorDecoder {
    public static final String INVALID_REQUEST = "Missing or invalid request parameters";
    public static final String SERVER_ERROR = "Server error";

    @Override
    public Exception decode(String methodKey, Response response) {
        String responseBody = null;
        Integer status = response.status();

        try {
            responseBody = getResponseBody(response);
            log.error("Response from FH failed with error code {}, error message {}", status, responseBody);
        } catch (IOException e) {
            log.error("Error while reading the response:{}", e.getMessage());
        }
        if (String.valueOf(response.status()).startsWith("4")) {
            return new BadFutureHearingRequestException(INVALID_REQUEST, status, responseBody);
        } else {
            return new FutureHearingServerException(SERVER_ERROR);
        }
    }

    private String getResponseBody(Response response) throws IOException {
        return new BufferedReader(new InputStreamReader(response.body().asInputStream()))
            .lines().parallel().collect(Collectors.joining("\n"));
    }
}
