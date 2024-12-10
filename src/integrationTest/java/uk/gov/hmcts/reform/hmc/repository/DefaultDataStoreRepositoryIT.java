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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubReturn200ForAllCasesFromDataStore;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubReturn400AllForCasesFromDataStore;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_NOT_FOUND;

public class DefaultDataStoreRepositoryIT extends BaseTest {

    @Autowired
    private DefaultDataStoreRepository defaultDataStoreRepository;

    public static final String CASE_TYPE = "CaseType1";

    List<String> caseRefs = Arrays.asList("9372710950276233", "9856815055686759");

    @Nested
    @DisplayName("Find All Cases By Case Id Using External Api")
    class FindAllCasesByCaseIdUsingExternalApi {
        @Test
        void shouldSuccessfullyGetAllCasesFromDataStore() {
            stubReturn200ForAllCasesFromDataStore(caseRefs, caseRefs);
            CaseSearchResult result = defaultDataStoreRepository.findAllCasesByCaseIdUsingExternalApi(CASE_TYPE,
                                                                      TestingUtil.createSearchQuery(caseRefs));
            assertThat(result.getCases().size(), is(2));
            assertThat(result.getCases().get(0).getId(), is("9372710950276233"));
            assertThat(result.getCases().get(1).getId(), is("9856815055686759"));
        }

        @Test
        void shouldSuccessfullyGetValidCasesFromDataStore() {
            stubReturn200ForAllCasesFromDataStore(caseRefs, Arrays.asList(caseRefs.get(1)));
            CaseSearchResult result = defaultDataStoreRepository.findAllCasesByCaseIdUsingExternalApi(CASE_TYPE,
                                                         TestingUtil.createSearchQuery(caseRefs));
            assertThat(result.getCases().size(), is(1));
            assertThat(result.getCases().get(0).getId(), is("9856815055686759"));
        }

        @Test
        void test400GetAllCasesFromDataStore() {
            stubReturn400AllForCasesFromDataStore(caseRefs, "");
            Exception exception = assertThrows(
                CaseCouldNotBeFoundException.class, () ->
                    defaultDataStoreRepository.findAllCasesByCaseIdUsingExternalApi(
                        "",
                        TestingUtil.createSearchQuery(caseRefs)));
            assertThat(exception.getMessage(), is(CASE_NOT_FOUND));
        }

    }
}
