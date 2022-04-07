package uk.gov.hmcts.reform.hmc.client.futurehearing;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(
    name = "hearing-management-interface-api",
    url = "${fh.hmi.host}",
    configuration = {FutureHearingApiClientConfig.class, HearingManagementInterfaceApiClientConfig.class}
)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public interface HearingManagementInterfaceApiClient {

    String HEARINGS_URL = "/resources/linked-hearing-group";

    @PostMapping(value = HEARINGS_URL, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    void createLinkedHearingGroup(@RequestHeader(AUTHORIZATION) String token,
                                                                @RequestBody JsonNode data);

}
