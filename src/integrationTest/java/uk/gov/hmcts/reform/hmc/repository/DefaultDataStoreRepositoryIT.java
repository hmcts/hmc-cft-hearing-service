package uk.gov.hmcts.reform.hmc.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.client.datastore.model.CaseSearchResult;
import uk.gov.hmcts.reform.hmc.exceptions.CaseCouldNotBeFoundException;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.util.Arrays;
import java.util.List;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubReturn200ForAllCasesFromDataStore;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubReturn4xxAllForCasesFromDataStore;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_NOT_FOUND;

public class DefaultDataStoreRepositoryIT extends BaseTest {

    @Autowired
    private DefaultDataStoreRepository defaultDataStoreRepository;

    public static final String CASE_TYPE = "CaseType1";
    public static final String UNAUTHORIZED_CASE_TYPE = "\"*(&\"";
    public static final String INVALID_CASE_TYPE = "invalidCaseType";

    @Nested
    @DisplayName("Find All Cases By Case Id Using External Api")
    class FindAllCasesByCaseIdUsingExternalApi {
        @Test
        void shouldSuccessfullyGetAllCasesFromDataStore() {
            List<String> caseRefs = Arrays.asList("9372710950276233", "9856815055686759");
            stubReturn200ForAllCasesFromDataStore(caseRefs, caseRefs, CASE_TYPE);
            CaseSearchResult result = defaultDataStoreRepository.findAllCasesByCaseIdUsingExternalApi(CASE_TYPE,
                                                                      TestingUtil.createSearchQuery(caseRefs));
            assertEquals(2, result.getCases().size());
            assertEquals("9372710950276233", result.getCases().get(0).getId());
            assertEquals("9856815055686759", result.getCases().get(1).getId());
        }

        @Test
        void shouldSuccessfullyGetValidCasesFromDataStore() {
            List<String> requestCaseRefs = Arrays.asList("9372710950276243", "9856815055686759");
            stubReturn200ForAllCasesFromDataStore(requestCaseRefs, Arrays.asList(requestCaseRefs.get(1)), CASE_TYPE);
            CaseSearchResult result = defaultDataStoreRepository.findAllCasesByCaseIdUsingExternalApi(CASE_TYPE,
                                                         TestingUtil.createSearchQuery(requestCaseRefs));
            assertEquals(1, result.getCases().size());
            assertEquals("9856815055686759", result.getCases().get(0).getId());
        }

        @Test
        void test403GetAllCasesFromDataStore() {
            List<String> caseRefs = Arrays.asList("9372710950276233", "9856815055686759");
            stubReturn4xxAllForCasesFromDataStore(caseRefs, UNAUTHORIZED_CASE_TYPE, HTTP_UNAUTHORIZED);
            Exception exception = assertThrows(
                CaseCouldNotBeFoundException.class, () ->
                    defaultDataStoreRepository.findAllCasesByCaseIdUsingExternalApi(
                        UNAUTHORIZED_CASE_TYPE,
                        TestingUtil.createSearchQuery(caseRefs)));
            assertEquals(CASE_NOT_FOUND, exception.getMessage());
        }

        @Test
        void test404GetAllCasesFromDataStore() {
            List<String> caseRefs = Arrays.asList("9372710950276233", "9856815055686759");
            stubReturn4xxAllForCasesFromDataStore(caseRefs, INVALID_CASE_TYPE, HTTP_NOT_FOUND);
            Exception exception = assertThrows(
                CaseCouldNotBeFoundException.class, () ->
                    defaultDataStoreRepository.findAllCasesByCaseIdUsingExternalApi(
                        INVALID_CASE_TYPE,
                        TestingUtil.createSearchQuery(caseRefs)));
            assertEquals(CASE_NOT_FOUND, exception.getMessage());
        }

    }
}
