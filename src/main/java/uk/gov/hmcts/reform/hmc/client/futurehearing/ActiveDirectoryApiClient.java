package uk.gov.hmcts.reform.hmc.client.futurehearing;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@FeignClient(
    name = "future-hearing-api",
    url = "${fh.ad.host}",
    configuration = {FutureHearingApiClientConfig.class}
)
public interface ActiveDirectoryApiClient {

    String GET_TOKEN = "${fh.ad.get-token-url}";

    @PostMapping(value = GET_TOKEN, consumes = APPLICATION_FORM_URLENCODED_VALUE)
    AuthenticationResponse authenticate(@RequestBody String authRequest);
}
