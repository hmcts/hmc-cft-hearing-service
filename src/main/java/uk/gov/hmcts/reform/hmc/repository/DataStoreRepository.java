package uk.gov.hmcts.reform.hmc.repository;

import uk.gov.hmcts.reform.hmc.client.datastore.model.CaseSearchResult;
import uk.gov.hmcts.reform.hmc.client.datastore.model.DataStoreCaseDetails;

public interface DataStoreRepository {

    DataStoreCaseDetails findCaseByCaseIdUsingExternalApi(String caseId);

    CaseSearchResult findAllCasesByCaseIdUsingExternalApi(String caseTypeId, String jsonSearchRequest);

}
