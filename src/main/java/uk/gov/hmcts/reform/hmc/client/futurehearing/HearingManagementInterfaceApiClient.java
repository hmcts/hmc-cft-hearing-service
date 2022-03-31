package uk.gov.hmcts.reform.hmc.client.futurehearing;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
    name = "hearing-management-interface-api",
    url = "${fh.hmi.host}",
    configuration = {FutureHearingApiClientConfig.class, HearingManagementInterfaceApiClientConfig.class}
)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public interface HearingManagementInterfaceApiClient {

    String HEARINGS_URL = "/resources/linked-hearing-group";

}
