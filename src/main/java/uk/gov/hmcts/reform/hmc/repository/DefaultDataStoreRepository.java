package uk.gov.hmcts.reform.hmc.repository;

import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.hmc.client.datastore.DataStoreApiClient;
import uk.gov.hmcts.reform.hmc.client.datastore.model.CaseSearchResult;
import uk.gov.hmcts.reform.hmc.client.datastore.model.DataStoreCaseDetails;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;
import uk.gov.hmcts.reform.hmc.exceptions.CaseCouldNotBeFoundException;

import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_NOT_FOUND;

@Repository("defaultDataStoreRepository")
public class DefaultDataStoreRepository implements DataStoreRepository {
    private final DataStoreApiClient dataStoreApi;
    protected final SecurityUtils securityUtils;

    @Autowired
    public DefaultDataStoreRepository(DataStoreApiClient dataStoreApi,
                                      SecurityUtils securityUtils) {
        this.dataStoreApi = dataStoreApi;
        this.securityUtils = securityUtils;
    }

    @Override
    public DataStoreCaseDetails findCaseByCaseIdUsingExternalApi(String caseId) {
        try {
            return dataStoreApi.getCaseDetailsByCaseIdViaExternalApi(securityUtils.getUserBearerToken(), caseId);
        } catch (FeignException e) {
            if (HttpStatus.NOT_FOUND.value() == e.status()) {
                throw new CaseCouldNotBeFoundException(CASE_NOT_FOUND);
            }
            throw e;
        }
    }

    @Override
    public CaseSearchResult findAllCasesByCaseIdUsingExternalApi(String caseTypeId,
                                                                 String jsonSearchRequest) {
        try {
            return dataStoreApi.getCaseDetailsForAllCaseRefsByCaseIdViaExternalApi(
                securityUtils.getUserBearerToken(), caseTypeId, jsonSearchRequest);
        } catch (FeignException e) {
            if (HttpStatus.UNAUTHORIZED.value() == e.status() || HttpStatus.NOT_FOUND.value() == e.status()) {
                throw new CaseCouldNotBeFoundException(CASE_NOT_FOUND);
            }
            throw e;
        }
    }
}
