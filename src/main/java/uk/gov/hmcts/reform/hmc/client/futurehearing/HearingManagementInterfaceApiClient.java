package uk.gov.hmcts.reform.hmc.client.futurehearing;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    String PRIVATE_HEALTH_URL = "/health";
    String DELETE_HEARINGS_URL = "/resources/linked-hearing-group/{groupClientReference}";
    String UPDATE_HEARINGS_URL = "/resources/linked-hearing-group/{groupClientReference}";
    String CREATE_HEARINGS_URL = "/resources/linked-hearing-group";

    @GetMapping(value = PRIVATE_HEALTH_URL, produces = APPLICATION_JSON_VALUE)
    HealthCheckResponse privateHealthCheck(@RequestHeader(AUTHORIZATION) String token);

    @DeleteMapping(value = DELETE_HEARINGS_URL, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    void deleteLinkedHearingGroup(@RequestHeader(AUTHORIZATION) String token,
                                      @PathVariable("groupClientReference")
                                                                    String requestId);

    @PutMapping(value = UPDATE_HEARINGS_URL, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    void updateLinkedHearingGroup(@RequestHeader(AUTHORIZATION) String token,
                                  @PathVariable("groupClientReference") String requestId,
                                  @RequestBody JsonNode data);

    @PostMapping(value = CREATE_HEARINGS_URL, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    void createLinkedHearingGroup(@RequestHeader(AUTHORIZATION) String token,  @RequestBody JsonNode data);


}
