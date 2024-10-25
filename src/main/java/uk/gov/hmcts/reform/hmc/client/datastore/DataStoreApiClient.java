package uk.gov.hmcts.reform.hmc.client.datastore;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.hmc.client.datastore.model.DataStoreCaseDetails;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@FeignClient(
    name = "${ccd.data-store.client-name:data-store-api}",
    url = "${ccd.data-store.host}",
    configuration = DataStoreApiClientConfig.class
)

public interface DataStoreApiClient {

    String CASE_ID = "caseId";
    String CASES_WITH_ID = "/cases/{caseId}";

    @GetMapping(CASES_WITH_ID)
    DataStoreCaseDetails getCaseDetailsByCaseIdViaExternalApi(
        @RequestHeader(AUTHORIZATION) String userAuthorizationHeader, @PathVariable(CASE_ID) String caseId);

}
