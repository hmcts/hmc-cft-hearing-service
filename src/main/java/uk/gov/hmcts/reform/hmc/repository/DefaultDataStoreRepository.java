package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.hmc.client.datastore.DataStoreApiClient;
import uk.gov.hmcts.reform.hmc.client.datastore.model.DataStoreCaseDetails;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;

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
        return dataStoreApi.getCaseDetailsByCaseIdViaExternalApi(securityUtils.getUserBearerToken(), caseId);
    }
}
