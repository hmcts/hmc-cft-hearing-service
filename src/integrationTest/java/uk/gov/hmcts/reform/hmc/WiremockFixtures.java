package uk.gov.hmcts.reform.hmc;

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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.reform.hmc.client.datastore.model.DataStoreCaseDetails;
import uk.gov.hmcts.reform.hmc.data.RoleAssignmentResponse;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class WiremockFixtures {

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

    public static void stubSuccessfullyValidateHearingObject(HearingRequest hearingRequest) {
        stubFor(WireMock.post(urlEqualTo("/hearing"))
                    .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE))
                    .withHeader(HttpHeaders.ACCEPT, equalTo(APPLICATION_JSON_VALUE))
                    .withRequestBody(
                        equalToJson(
                            getJsonString(hearingRequest)))
                    .willReturn(aResponse().withStatus(HTTP_CREATED)));
    }

    public static void stubReturn400WhileValidateHearingObject(HearingRequest hearingRequest) {
        stubFor(WireMock.post(urlEqualTo("/hearing"))
                    .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE))
                    .withHeader(HttpHeaders.ACCEPT, equalTo(APPLICATION_JSON_VALUE))
                    .withRequestBody(
                        equalToJson(
                            getJsonString(hearingRequest)))
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
