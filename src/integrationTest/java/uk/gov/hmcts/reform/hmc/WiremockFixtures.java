package uk.gov.hmcts.reform.hmc;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import uk.gov.hmcts.reform.hmc.client.datastore.model.DataStoreCaseDetails;
import uk.gov.hmcts.reform.hmc.client.futurehearing.HearingManagementInterfaceResponse;
import uk.gov.hmcts.reform.hmc.data.RoleAssignmentResponse;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class WiremockFixtures {

    private static final String CLIENT_ID = "CLIENT_ID";
    private static final String CLIENT_SECRET = "CLIENT_SECRET";
    private static final String SCOPE = "SCOPE";
    private static final String GRANT_TYPE = "GRANT_TYPE";
    private static final String GET_TOKEN_URL = "/FH_GET_TOKEN_URL";
    private static final String HMI_REQUEST_URL = "/resources/linked-hearing-group";
    private static final String SOURCE_SYSTEM = "SOURCE_SYSTEM";
    private static final String DESTINATION_SYSTEM = "DESTINATION_SYSTEM";

    public static String TEST_BODY = "This is a test message";

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
                .withHeader(HttpHeaders.CONNECTION, "close")
                .build();
        }
    }

    public static void stubDeleteMethodThrowingError(int status, String url) {
        stubFor(WireMock.delete(urlEqualTo(url))
                    .willReturn(okJson(TEST_BODY).withStatus(status)));
    }

    public static void stubSuccessfullyDeleteLinkedHearingGroups(String token, String requestId) {
        HearingManagementInterfaceResponse response = new HearingManagementInterfaceResponse();
        response.setResponseCode(201);
        response.setDescription("The request was received successfully.");
        stubFor(WireMock.delete(urlEqualTo(HMI_REQUEST_URL + "/" + requestId))
                    .withHeader("Content-Type", equalTo(APPLICATION_JSON_VALUE))
                    .withHeader("Accept", equalTo(APPLICATION_JSON_VALUE))
                    .withHeader("Source-System", equalTo(SOURCE_SYSTEM))
                    .withHeader("Destination-System", equalTo(DESTINATION_SYSTEM))
                    .withHeader("Request-Created-At", matching("^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]"
                                                                   + "{2}:[0-9]{2}.[0-9]{6}Z"))
                    .withHeader(AUTHORIZATION, equalTo("Bearer " + token))
                    .willReturn(aResponse()
                                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                    .withStatus(201)
                    ));
    }

    public static void stubSuccessfullyValidateHearingObject(HearingRequest createHearingRequest) {
        stubFor(WireMock.post(urlEqualTo("/hearing"))
                    .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE))
                    .withHeader(HttpHeaders.ACCEPT, equalTo(APPLICATION_JSON_VALUE))
                    .withRequestBody(
                        equalToJson(
                            getJsonString(createHearingRequest)))
                    .willReturn(aResponse().withStatus(HTTP_CREATED)));
    }

    public static void stubReturn400WhileValidateHearingObject(HearingRequest createHearingRequest) {
        stubFor(WireMock.post(urlEqualTo("/hearing"))
                    .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE))
                    .withHeader(HttpHeaders.ACCEPT, equalTo(APPLICATION_JSON_VALUE))
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
                                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));
    }

    public static void stubReturn200CaseDetailsByCaseId(String caseReference, DataStoreCaseDetails caseDetails) {
        stubFor(WireMock.get(urlEqualTo("/cases/" + caseReference))
                    .willReturn(aResponse()
                                    .withStatus(HTTP_OK)
                                    .withBody(getJsonString(caseDetails))
                                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));
    }

    public static void stubReturn404FromDataStore(String caseReference) {
        stubFor(WireMock.get(urlEqualTo("/cases/" + caseReference))
                    .willReturn(aResponse()
                                    .withStatus(HTTP_NOT_FOUND)
                                    .withBody(getJsonString("No case found"))
                                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

    }

    @SuppressWarnings({"PMD.AvoidThrowingRawExceptionTypes", "squid:S112"})
    // Required as wiremock's Json.getObjectMapper().registerModule(..); not working
    // see https://github.com/tomakehurst/wiremock/issues/1127
    private static String getJsonString(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
