package uk.gov.hmcts.reform.hmc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.Metadata;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.reform.hmc.client.datastore.model.CaseSearchResult;
import uk.gov.hmcts.reform.hmc.client.datastore.model.DataStoreCaseDetails;
import uk.gov.hmcts.reform.hmc.client.futurehearing.AuthenticationResponse;
import uk.gov.hmcts.reform.hmc.data.RoleAssignmentResponse;
import uk.gov.hmcts.reform.hmc.model.HearingManagementInterfaceResponse;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.jsonResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.removeStubsByMetadata;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_MULT_CHOICE;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONNECTION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.LIST_ASSIST_FAILED_TO_RESPOND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.REJECTED_BY_LIST_ASSIST;

public class WiremockFixtures {

    private static final String CLIENT_ID = "CLIENT_ID";
    private static final String CLIENT_SECRET = "CLIENT_SECRET";
    private static final String SCOPE = "SCOPE";
    private static final String GRANT_TYPE = "GRANT_TYPE";
    private static final String GET_TOKEN_URL = "/FH_GET_TOKEN_URL";
    private static final String HMI_REQUEST_URL = "/resources/linked-hearing-group";
    private static final String HMI_PRIVATE_HEALTH_URL = "/health";
    private static final String SOURCE_SYSTEM = "SOURCE_SYSTEM";
    private static final String DESTINATION_SYSTEM = "DESTINATION_SYSTEM";

    private static final String HEADER_SOURCE_SYSTEM = "Source-System";
    private static final String HEADER_DESTINATION_SYSTEM = "Destination-System";
    private static final String HEADER_REQUEST_CREATED_AT = "Request-Created-At";

    private static final String REGEX_TIMESTAMP = "^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z";

    private static final String STUB_TAG_FAIL_TO_RETURN_TOKEN = "fail-to-return-token";
    private static final String STUB_TAG_FAIL_TO_RETURN_TOKEN_TIMEOUT = "fail-to-return-token-timeout";

    public static final String TEST_BODY = "This is a test message";
    public static final String CASE_TYPE = "CaseType1";
    public static final String JURISDICTION = "Jurisdiction1";

    private static final ObjectMapper OBJECT_MAPPER = new Jackson2ObjectMapperBuilder()
        .modules(new Jdk8Module(), new JavaTimeModule())
        .build();

    private WiremockFixtures() {
    }

    // Same issue as here https://github.com/tomakehurst/wiremock/issues/97
    public static class ConnectionClosedTransformer extends ResponseDefinitionTransformer {

        @Override
        public String getName() {
            return "keep-alive-disabler";
        }

        @Override
        public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition,
                                            FileSource files, Parameters parameters) {
            return ResponseDefinitionBuilder.like(responseDefinition)
                .withHeader(CONNECTION, "close")
                .build();
        }
    }

    public static void stubSuccessfullyReturnToken(String token) {
        AuthenticationResponse authenticationResponse = new AuthenticationResponse();
        authenticationResponse.setAccessToken(token);
        stubFor(WireMock.post(urlEqualTo(GET_TOKEN_URL))
                    .withHeader(CONTENT_TYPE, equalTo(APPLICATION_FORM_URLENCODED_VALUE))
                    .withRequestBody(matching("grant_type=" + GRANT_TYPE + "&client_id=" + CLIENT_ID + "&scope="
                                                  + SCOPE + "&client_secret=" + CLIENT_SECRET))
                    .willReturn(aResponse()
                                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                    .withBody(getJsonString(authenticationResponse))
                                    .withStatus(200)
                    ));
    }

    public static void stubFailToReturnToken(int status, String errorDescription, List<Integer> errorCodes) {
        StringJoiner joiner = new StringJoiner(",");
        errorCodes.forEach(value -> joiner.add(String.valueOf(value)));

        String adErrorResponse = """
            {
                "error_description":"%s",
                "error_codes":[%s]
            }""".formatted(errorDescription, joiner.toString());

        stubFor(WireMock.post(urlEqualTo(GET_TOKEN_URL))
                    .withMetadata(Metadata.metadata().attr("tag", STUB_TAG_FAIL_TO_RETURN_TOKEN))
                    .withHeader(CONTENT_TYPE, equalTo(APPLICATION_FORM_URLENCODED_VALUE))
                    .withRequestBody(matching("grant_type=" + GRANT_TYPE + "&client_id=" + CLIENT_ID + "&scope="
                                                  + SCOPE + "&client_secret=" + CLIENT_SECRET))
                    .willReturn(jsonResponse(adErrorResponse, status))
        );
    }

    public static void removeStubFailToReturnToken() {
        removeStubsByMetadata(matchingJsonPath("$.tag", equalTo(STUB_TAG_FAIL_TO_RETURN_TOKEN)));
    }

    public static void stubFailToReturnTokenTimeout() {
        // Note: This needs to be used in conjunction with a smaller than the default
        // value for the feign client readTimeout property to trigger a timeout
        stubFor(post(urlEqualTo(GET_TOKEN_URL))
                    .withHeader(CONTENT_TYPE, equalTo(APPLICATION_FORM_URLENCODED_VALUE))
                    .withMetadata(Metadata.metadata().attr("tag", STUB_TAG_FAIL_TO_RETURN_TOKEN_TIMEOUT))
                    .withRequestBody(matching("grant_type=" + GRANT_TYPE + "&client_id=" + CLIENT_ID + "&scope="
                                                  + SCOPE + "&client_secret=" + CLIENT_SECRET))
                    .willReturn(aResponse()
                                    .withFixedDelay(10000))
        );
    }

    public static void removeStubFailToReturnTokenTimeout() {
        removeStubsByMetadata(matchingJsonPath("$.tag", equalTo(STUB_TAG_FAIL_TO_RETURN_TOKEN_TIMEOUT)));
    }

    public static void stubPostMethodThrowingAuthenticationError(int status, String url) {
        stubFor(WireMock.post(urlEqualTo(url))
                    .willReturn(okJson(TEST_BODY).withStatus(status)));
    }

    public static void stubDeleteMethodThrowingError(int status, String url) {
        stubFor(WireMock.delete(urlEqualTo(url))
                    .willReturn(okJson(TEST_BODY).withStatus(status)));
    }

    public static void stubUpdateMethodThrowingError(int status, String url) {
        stubFor(WireMock.put(urlEqualTo(url))
                    .willReturn(okJson(TEST_BODY).withStatus(status)));
    }

    public static void stubPostCreateLinkHearingGroup(int status, String url, String token) {
        HearingManagementInterfaceResponse response = new HearingManagementInterfaceResponse();
        response.setResponseCode(status);
        response.setDescription("The request was received successfully.");
        stubFor(WireMock.post(urlEqualTo(url))
                    .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE))
                    .withHeader(ACCEPT, equalTo(APPLICATION_JSON_VALUE))
                    .withHeader(HEADER_SOURCE_SYSTEM, equalTo(SOURCE_SYSTEM))
                    .withHeader(HEADER_DESTINATION_SYSTEM, equalTo(DESTINATION_SYSTEM))
                    .withHeader(HEADER_REQUEST_CREATED_AT, matching(REGEX_TIMESTAMP))
                    .withHeader(AUTHORIZATION, equalTo("Bearer " + token))
                    .willReturn(aResponse()
                                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                    .withBody(getJsonString(response))
                                    .withStatus(status)
                    ));
    }

    public static void stubPutUpdateLinkHearingGroup(int status, String requestId, String token) {
        HearingManagementInterfaceResponse response = new HearingManagementInterfaceResponse();
        response.setResponseCode(status);
        response.setDescription("The request was received successfully.");
        stubFor(WireMock.put(urlEqualTo(HMI_REQUEST_URL + "/" + requestId))
                    .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE))
                    .withHeader(ACCEPT, equalTo(APPLICATION_JSON_VALUE))
                    .withHeader(HEADER_SOURCE_SYSTEM, equalTo(SOURCE_SYSTEM))
                    .withHeader(HEADER_DESTINATION_SYSTEM, equalTo(DESTINATION_SYSTEM))
                    .withHeader(HEADER_REQUEST_CREATED_AT, matching(REGEX_TIMESTAMP))
                    .withHeader(AUTHORIZATION, equalTo("Bearer " + token))
                    .willReturn(aResponse()
                                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                    .withBody(getJsonString(response))
                                    .withStatus(status)
                    ));
    }

    public static void stubSuccessfullyDeleteLinkedHearingGroups(String token, String requestId) {
        stubFor(WireMock.delete(urlEqualTo(HMI_REQUEST_URL + "/" + requestId))
                    .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE))
                    .withHeader(ACCEPT, equalTo(APPLICATION_JSON_VALUE))
                    .withHeader(HEADER_SOURCE_SYSTEM, equalTo(SOURCE_SYSTEM))
                    .withHeader(HEADER_DESTINATION_SYSTEM, equalTo(DESTINATION_SYSTEM))
                    .withHeader(HEADER_REQUEST_CREATED_AT, matching(REGEX_TIMESTAMP))
                    .withHeader(AUTHORIZATION, equalTo("Bearer " + token))
                    .willReturn(aResponse()
                                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                    .withStatus(HTTP_OK)
                    ));
    }

    public static void stubDeleteLinkedHearingGroupsReturn4XX(String token, String requestId) {
        HearingManagementInterfaceResponse response = new HearingManagementInterfaceResponse();
        response.setResponseCode(400);
        response.setDescription(REJECTED_BY_LIST_ASSIST);
        stubFor(WireMock.delete(urlEqualTo(HMI_REQUEST_URL + "/" + requestId))
                    .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE))
                    .withHeader(ACCEPT, equalTo(APPLICATION_JSON_VALUE))
                    .withHeader(HEADER_SOURCE_SYSTEM, equalTo(SOURCE_SYSTEM))
                    .withHeader(HEADER_DESTINATION_SYSTEM, equalTo(DESTINATION_SYSTEM))
                    .withHeader(HEADER_REQUEST_CREATED_AT, matching(REGEX_TIMESTAMP))
                    .withHeader(AUTHORIZATION, equalTo("Bearer " + token))
                    .willReturn(aResponse()
                                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                    .withBody(getJsonString(response))
                                    .withStatus(HTTP_BAD_REQUEST)
                    ));
    }

    public static void stubDeleteLinkedHearingGroupsReturn404(String token, String requestId) {
        HearingManagementInterfaceResponse response = new HearingManagementInterfaceResponse();
        response.setResponseCode(404);
        response.setDescription(REJECTED_BY_LIST_ASSIST);
        stubFor(WireMock.delete(urlEqualTo(HMI_REQUEST_URL + "/" + requestId))
                    .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE))
                    .withHeader(ACCEPT, equalTo(APPLICATION_JSON_VALUE))
                    .withHeader(HEADER_SOURCE_SYSTEM, equalTo(SOURCE_SYSTEM))
                    .withHeader(HEADER_DESTINATION_SYSTEM, equalTo(DESTINATION_SYSTEM))
                    .withHeader(HEADER_REQUEST_CREATED_AT, matching(REGEX_TIMESTAMP))
                    .withHeader(AUTHORIZATION, equalTo("Bearer " + token))
                    .willReturn(aResponse()
                                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                    .withBody(getJsonString(response))
                                    .withStatus(HTTP_NOT_FOUND)
                    ));
    }

    public static void stubDeleteLinkedHearingGroupsReturn3XX(String token, String requestId) {
        HearingManagementInterfaceResponse response = new HearingManagementInterfaceResponse();
        response.setResponseCode(500);
        response.setDescription(LIST_ASSIST_FAILED_TO_RESPOND);
        stubFor(WireMock.delete(urlEqualTo(HMI_REQUEST_URL + "/" + requestId))
                    .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE))
                    .withHeader(ACCEPT, equalTo(APPLICATION_JSON_VALUE))
                    .withHeader(HEADER_SOURCE_SYSTEM, equalTo(SOURCE_SYSTEM))
                    .withHeader(HEADER_DESTINATION_SYSTEM, equalTo(DESTINATION_SYSTEM))
                    .withHeader(HEADER_REQUEST_CREATED_AT, matching(REGEX_TIMESTAMP))
                    .withHeader(AUTHORIZATION, equalTo("Bearer " + token))
                    .willReturn(aResponse()
                                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                    .withBody(getJsonString(response))
                                    .withStatus(HTTP_MULT_CHOICE)
                    ));
    }

    public static void stubDeleteLinkedHearingGroupsReturn5XX(String token, String requestId) {
        HearingManagementInterfaceResponse response = new HearingManagementInterfaceResponse();
        response.setResponseCode(500);
        response.setDescription(LIST_ASSIST_FAILED_TO_RESPOND);
        stubFor(WireMock.delete(urlEqualTo(HMI_REQUEST_URL + "/" + requestId))
                    .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE))
                    .withHeader(ACCEPT, equalTo(APPLICATION_JSON_VALUE))
                    .withHeader(HEADER_SOURCE_SYSTEM, equalTo(SOURCE_SYSTEM))
                    .withHeader(HEADER_DESTINATION_SYSTEM, equalTo(DESTINATION_SYSTEM))
                    .withHeader(HEADER_REQUEST_CREATED_AT, matching(REGEX_TIMESTAMP))
                    .withHeader(AUTHORIZATION, equalTo("Bearer " + token))
                    .willReturn(aResponse()
                                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                    .withBody(getJsonString(response))
                                    .withStatus(HTTP_INTERNAL_ERROR)
                    ));
    }

    public static void stubHealthCheck(String token, Status healthStatus) {
        stubFor(get(urlEqualTo(HMI_PRIVATE_HEALTH_URL))
                    .withHeader(ACCEPT, equalTo(APPLICATION_JSON_VALUE))
                    .withHeader(HEADER_SOURCE_SYSTEM, equalTo(SOURCE_SYSTEM))
                    .withHeader(HEADER_DESTINATION_SYSTEM, equalTo(DESTINATION_SYSTEM))
                    .withHeader(HEADER_REQUEST_CREATED_AT, matching(REGEX_TIMESTAMP))
                    .withHeader(AUTHORIZATION, equalTo("Bearer " + token))
                    .willReturn(aResponse()
                                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                    .withBody(getJsonString(healthStatus))
                                    .withStatus(200)
                    ));
    }

    public static void stubHealthCheckThrowingError(String token, int status, String message) {
        String hmiErrorResponse = """
            {
                "statusCode": %d,
                "message": "%s"
            }""".formatted(status, message);
        stubFor(get(urlEqualTo(HMI_PRIVATE_HEALTH_URL))
                    .withHeader(ACCEPT, equalTo(APPLICATION_JSON_VALUE))
                    .withHeader(HEADER_SOURCE_SYSTEM, equalTo(SOURCE_SYSTEM))
                    .withHeader(HEADER_DESTINATION_SYSTEM, equalTo(DESTINATION_SYSTEM))
                    .withHeader(HEADER_REQUEST_CREATED_AT, matching(REGEX_TIMESTAMP))
                    .withHeader(AUTHORIZATION, equalTo("Bearer " + token))
                    .willReturn(jsonResponse(hmiErrorResponse, status))
        );
    }

    public static void stubSuccessfullyValidateHearingObject(HearingRequest createHearingRequest) {
        stubFor(WireMock.post(urlEqualTo("/hearing"))
                    .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE))
                    .withHeader(ACCEPT, equalTo(APPLICATION_JSON_VALUE))
                    .withRequestBody(
                        equalToJson(
                            getJsonString(createHearingRequest)))
                    .willReturn(aResponse().withStatus(HTTP_CREATED)));
    }

    public static void stubReturn400WhileValidateHearingObject(HearingRequest createHearingRequest) {
        stubFor(WireMock.post(urlEqualTo("/hearing"))
                    .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE))
                    .withHeader(ACCEPT, equalTo(APPLICATION_JSON_VALUE))
                    .withRequestBody(
                        equalToJson(
                            getJsonString(createHearingRequest)))
                    .willReturn(aResponse().withStatus(HTTP_BAD_REQUEST)));
    }

    public static void stubReturn200RoleAssignments(String userId, RoleAssignmentResponse roleAssignment) {
        stubFor(WireMock.get(urlEqualTo("/am/role-assignments/actors/" + userId))
                    .willReturn(aResponse()
                                    .withStatus(HTTP_OK)
                                    .withBody(getJsonString(roleAssignment))
                                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
    }

    public static void stubReturn200CaseDetailsByCaseId(String caseReference, DataStoreCaseDetails caseDetails) {
        stubFor(WireMock.get(urlEqualTo("/cases/" + caseReference))
                    .willReturn(aResponse()
                                    .withStatus(HTTP_OK)
                                    .withBody(getJsonString(caseDetails))
                                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
    }

    public static void stubReturn404FromDataStore(String caseReference) {
        stubFor(WireMock.get(urlEqualTo("/cases/" + caseReference))
                    .willReturn(aResponse()
                                    .withStatus(HTTP_NOT_FOUND)
                                    .withBody(getJsonString("No case found"))
                                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));

    }

    public static void stubReturn200ForAllCasesFromDataStore(List<String> caseRefs, List<String> caseRefsFromES) {
        stubFor(WireMock.post(urlEqualTo("/searchCases" + "?ctid=" + CASE_TYPE))
                    .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE))
                    .withHeader(ACCEPT, equalTo(APPLICATION_JSON_VALUE))
                    .withRequestBody(
                        equalToJson(TestingUtil.createSearchQuery(caseRefs)))
                    .willReturn(aResponse()
                                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                    .withBody(getJsonString(getCaseSearchResult(caseRefsFromES)))
                                    .withStatus(HTTP_OK)));
    }

    public static void stubReturn400AllForCasesFromDataStore(List<String> caseRefs, String caseType) {
        stubFor(WireMock.post(urlEqualTo("/searchCases" + "?ctid=" + caseType))
                    .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE))
                    .withHeader(ACCEPT, equalTo(APPLICATION_JSON_VALUE))
                    .withRequestBody(
                        equalToJson(TestingUtil.createSearchQuery(caseRefs)))
                    .willReturn(aResponse()
                                    .withStatus(HTTP_BAD_REQUEST)
                                    .withBody(getJsonString("Case type could not be found"))
                                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));

    }

    public static void stubReturn200ForAllCasesFromDataStorePaginated(Integer pageSize,
                                                                      Integer offset,
                                                                      List<String> caseReferences,
                                                                      String caseType,
                                                                      List<String> esCaseReferences) {
        stubFor(post(urlEqualTo("/searchCases" + "?ctid=" + caseType))
                    .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE))
                    .withHeader(ACCEPT, equalTo(APPLICATION_JSON_VALUE))
                    .withRequestBody(equalToJson(TestingUtil.createSearchQueryPaginated(pageSize,
                                                                                        offset,
                                                                                        caseReferences)))
                    .willReturn(aResponse()
                                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                    .withStatus(HTTP_OK)
                                    .withBody(getJsonString(getCaseSearchResult(esCaseReferences)))));
    }

    private static CaseSearchResult getCaseSearchResult(List<String> caseRefs) {
        List<DataStoreCaseDetails> caseDetailsList = new ArrayList<>();
        for (String caseRef : caseRefs) {
            DataStoreCaseDetails dataStoreCaseDetails = DataStoreCaseDetails.builder()
                .id(caseRef)
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .build();
            caseDetailsList.add(dataStoreCaseDetails);
        }
        return CaseSearchResult.builder()
            .cases(caseDetailsList)
            .build();
    }

    @SuppressWarnings({"PMD.AvoidThrowingRawExceptionTypes", "squid:S112"})
    // Required as wiremock's Json.getObjectMapper().registerModule(..); not working
    // see https://github.com/tomakehurst/wiremock/issues/1127
    public static String getJsonString(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
