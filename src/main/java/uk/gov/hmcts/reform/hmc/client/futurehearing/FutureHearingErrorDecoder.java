package uk.gov.hmcts.reform.hmc.client.futurehearing;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.hmc.client.hmi.ErrorDetails;
import uk.gov.hmcts.reform.hmc.exceptions.AuthenticationException;
import uk.gov.hmcts.reform.hmc.exceptions.BadFutureHearingRequestException;


@Slf4j
public class FutureHearingErrorDecoder implements ErrorDecoder {
    public static final String INVALID_REQUEST = "Missing or invalid request parameters";
    public static final String SERVER_ERROR = "Server error";

    @Override
    public Exception decode(String methodKey, Response response) {
        ErrorDetails errorDetails = new ErrorDetails();
        errorDetails.setErrorCode(response.status());
        log.error(String.format(
            "Response from FH failed with error code %s",errorDetails.getErrorCode()));

        if (String.valueOf(response.status()).startsWith("4")) {
            return new BadFutureHearingRequestException(INVALID_REQUEST, errorDetails);
        } else {
            return new AuthenticationException(SERVER_ERROR, errorDetails);
        }
    }
}
