package uk.gov.hmcts.reform.hmc.repository;

import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.hmc.client.datastore.DataStoreApiClient;
import uk.gov.hmcts.reform.hmc.client.datastore.model.CaseSearchResult;
import uk.gov.hmcts.reform.hmc.client.datastore.model.DataStoreCaseDetails;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;
import uk.gov.hmcts.reform.hmc.exceptions.CaseCouldNotBeFoundException;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_NOT_FOUND;

class DataStoreRepositoryTest {

    private static final String CASE_TYPE_ID = "TEST_CASE_TYPE";
    private static final String CASE_ID = "12345678";

    public static final String USER_TOKEN = "Bearer user Token";
    public static final String SYSTEM_USER_TOKEN = "Bearer system user Token";

    @Mock
    private DataStoreApiClient dataStoreApi;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private DefaultDataStoreRepository repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        given(securityUtils.getUserBearerToken()).willReturn(SYSTEM_USER_TOKEN);
    }

    @Test
    @DisplayName("find case by id as an invoking user using external facing API")
    void shouldFindCaseByCaseIdUsingExternalApi() {
        // ARRANGE
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .caseTypeId(CASE_TYPE_ID)
            .id(CASE_ID)
            .build();
        given(securityUtils.getUserBearerToken()).willReturn(USER_TOKEN);
        given(dataStoreApi.getCaseDetailsByCaseIdViaExternalApi(USER_TOKEN, CASE_ID)).willReturn(caseDetails);

        // ACT
        DataStoreCaseDetails result = repository.findCaseByCaseIdUsingExternalApi(CASE_ID);

        // ASSERT
        assertThat(result).isEqualTo(caseDetails);
        verify(dataStoreApi).getCaseDetailsByCaseIdViaExternalApi(eq(USER_TOKEN), eq(CASE_ID));
    }

    @Test
    @DisplayName("find case by id as an invoking user using external facing API return no cases")
    void shouldReturnNoCaseForFindCaseByCaseIdUsingExternalApi() {
        // ARRANGE
        given(securityUtils.getUserBearerToken()).willReturn(USER_TOKEN);
        given(dataStoreApi.getCaseDetailsByCaseIdViaExternalApi(USER_TOKEN, CASE_ID)).willReturn(null);

        // ACT
        DataStoreCaseDetails result = repository.findCaseByCaseIdUsingExternalApi(CASE_ID);

        // ASSERT
        assertThat(result).isNull();
        verify(dataStoreApi).getCaseDetailsByCaseIdViaExternalApi(eq(USER_TOKEN), eq(CASE_ID));
    }

    @Test
    @DisplayName("find case by id as an invoking user using external facing API throws CaseCouldNotBeFetchedException")
    void shouldThrowCaseCouldNotBeFetchedExceptionForFindCaseByCaseIdUsingExternalApi() {
        // ARRANGE
        given(securityUtils.getUserBearerToken()).willReturn(USER_TOKEN);
        given(dataStoreApi.getCaseDetailsByCaseIdViaExternalApi(
            USER_TOKEN,
            CASE_ID
        )).willThrow(new FeignException.NotFound("404",
                                                 Request.create(Request.HttpMethod.GET, "someUrl", Map.of(),
                                                                null, Charset.defaultCharset(),
                                                                null), null,null
        ));

        // ACT & ASSERT
        assertThatThrownBy(() -> repository.findCaseByCaseIdUsingExternalApi(CASE_ID))
            .isInstanceOf(CaseCouldNotBeFoundException.class)
            .hasMessageContaining(CASE_NOT_FOUND);
    }

    @Test
    @DisplayName("find all cases by ids as an invoking user using external facing API")
    void shouldFindAllCaseByCaseIdsUsingExternalApi() {
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .caseTypeId(CASE_TYPE_ID)
            .id(CASE_ID)
            .build();
        CaseSearchResult caseSearchResult = CaseSearchResult.builder()
            .cases(Arrays.asList(caseDetails))
            .build();
        given(securityUtils.getUserBearerToken()).willReturn(USER_TOKEN);
        given(dataStoreApi.getCaseDetailsForAllCaseRefsByCaseIdViaExternalApi(USER_TOKEN, CASE_TYPE_ID,
                                 "searchRequest")).willReturn(caseSearchResult);
        CaseSearchResult result = repository.findAllCasesByCaseIdUsingExternalApi(CASE_TYPE_ID,
                                                                                      "searchRequest");
        assertThat(result).isEqualTo(caseSearchResult);
        verify(dataStoreApi).getCaseDetailsForAllCaseRefsByCaseIdViaExternalApi(eq(USER_TOKEN), eq(CASE_TYPE_ID),
                                                                                eq("searchRequest"));
    }

    @Test
    @DisplayName("Find all cases as an invoking user using external facing API throws CaseCouldNotBeFoundException")
    void shouldThrow404ForFindAllCaseByCaseIdsUsingExternalApi() {
        given(securityUtils.getUserBearerToken()).willReturn(USER_TOKEN);
        given(dataStoreApi.getCaseDetailsForAllCaseRefsByCaseIdViaExternalApi(USER_TOKEN, CASE_TYPE_ID,
                                                                              "searchRequest"
        )).willThrow(new FeignException.NotFound("404",
                                                 Request.create(Request.HttpMethod.POST, "someUrl", Map.of(),
                                                                null, Charset.defaultCharset(),
                                                                null
                                                 ), null, null));
        assertThatThrownBy(() -> repository.findAllCasesByCaseIdUsingExternalApi(
            CASE_TYPE_ID, "searchRequest"))
            .isInstanceOf(CaseCouldNotBeFoundException.class)
            .hasMessageContaining(CASE_NOT_FOUND);
    }

    @Test
    @DisplayName("Find all cases as an invoking user using external facing API return no cases")
    void shouldReturnNoCaseFindAllCaseByCaseIdsUsingExternalApi() {
        given(securityUtils.getUserBearerToken()).willReturn(USER_TOKEN);
        given(dataStoreApi.getCaseDetailsForAllCaseRefsByCaseIdViaExternalApi(USER_TOKEN, CASE_TYPE_ID,
                                                                              "searchRequest"
        )).willReturn(null);
        CaseSearchResult result = repository.findAllCasesByCaseIdUsingExternalApi(CASE_TYPE_ID,
                                                                                  "searchRequest");
        assertThat(result).isNull();
        verify(dataStoreApi).getCaseDetailsForAllCaseRefsByCaseIdViaExternalApi(eq(USER_TOKEN), eq(CASE_TYPE_ID),
                                                                                eq("searchRequest"));
    }


}

