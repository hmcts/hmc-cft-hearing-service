package uk.gov.hmcts.reform.hmc.client.futurehearing;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    String DELETE_HEARINGS_URL = "/resources/linkedHearingGroup/{groupClientReference}";

    @DeleteMapping(value = DELETE_HEARINGS_URL, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    void deleteLinkedHearingGroup(@RequestHeader(AUTHORIZATION) String token,
                                      @PathVariable("groupClientReference")
                                                                    String requestId);
}
