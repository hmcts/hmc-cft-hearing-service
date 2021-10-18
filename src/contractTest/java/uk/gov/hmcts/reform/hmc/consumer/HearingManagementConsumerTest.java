package uk.gov.hmcts.reform.hmc.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslJsonRootValue;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.google.common.collect.ImmutableMap;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(PactConsumerTestExt.class)
public class HearingManagementConsumerTest {

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private static final String IDAM_OAUTH2_TOKEN = "pact-test-idam-token";
    private static final String SERVICE_AUTHORIZATION_TOKEN = "pact-test-s2s-token";

    static Map<String, String> headers = ImmutableMap.of(
        HttpHeaders.AUTHORIZATION, IDAM_OAUTH2_TOKEN,
        SERVICE_AUTHORIZATION, SERVICE_AUTHORIZATION_TOKEN
    );

    /**
     * create Hearing - send valid request.
     * @param builder Builder object
     * @return response Response object
     * @throws Exception exception
     */
    @Pact(provider = "mca", consumer = "mca_example_consumer")
    public RequestResponsePact createHearing(PactDslWithProvider builder) throws Exception {
        return builder
            .given("MCA successfully returns created hearing")
            .uponReceiving("Request to create hearing")
            .path("/hearing")
            .method(HttpMethod.POST.toString())
            .body(createValidCreateHearingRequest(), ContentType.APPLICATION_JSON)
            .headers(headers)
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .body(createCreateHearingResponse())
            .toPact();
    }

    /**
     * validation error from create Hearing - send faulty request.
     * @param builder builder object
     * @return response RequestResponsePact
     * @throws Exception exception
     */
    @Pact(provider = "mca", consumer = "mca_example_consumer")
    public RequestResponsePact validationErrorFromCreateHearing(PactDslWithProvider builder) throws Exception {
        return builder
            .given("MCA throws validation error for createHearing")
            .uponReceiving("Request to create hearing")
            .path("/hearing")
            .method(HttpMethod.POST.toString())
            .body(createInvalidCreateHearingRequest(), ContentType.APPLICATION_JSON)
            .headers(headers)
            .willRespondWith()
            .status(HttpStatus.BAD_REQUEST.value())
            .body(new PactDslJsonBody()
                      .stringType("message", "Intended assignee has to be in the same organisation")
                      .stringValue("status", "BAD_REQUEST")
                      .eachLike("errors", 1)
                      .closeArray()
            )
            .toPact();
    }

    /**
     * test expects to return the created hearing.
     * @param mockServer MockServer
     * @throws Exception exception
     */
    @Test
    @PactTestFor(pactMethod = "createHearing")
    public void shouldReturnCreatedHearing(MockServer mockServer) throws Exception {
        JsonPath response = RestAssured
            .given()
            .headers(headers)
            .when()
            .get(mockServer.getUrl() + "/hearing")
            .then()
            .statusCode(200)
            .and()
            .extract()
            .body()
            .jsonPath();

        // assertThat(response.getString("status_message"))
        //     .isEqualTo("Case-User-Role assignments returned successfully");
        // assertThat(response.getString("case_assignments[0].case_id"))
        //     .isEqualTo("1588234985453946");
        // assertThat(response.getString("case_assignments[0].shared_with[0].email"))
        //     .isEqualTo("John.Smith@gmail.com");
    }

    /**
     * test expects an error 400.
     * @param mockServer MockServer object
     * @throws Exception exception
     */
    @Test
    @PactTestFor(pactMethod = "validationErrorFromCreatingHearing")
    public void shouldReturn400BadRequestForCreateHearing(MockServer mockServer) throws Exception {
        JsonPath response = RestAssured
            .given()
            .headers(headers)
            .contentType(io.restassured.http.ContentType.JSON)
            .body(createInvalidCreateHearingRequest())
            .when()
            .post(mockServer.getUrl() + "/hearing")
            .then()
            .statusCode(400)
            .and()
            .extract()
            .body()
            .jsonPath();

        assertThat(response.getString("message")).isEqualTo("Some error message to be determined");
        assertThat(response.getString("status")).isEqualTo("BAD_REQUEST");
    }

    /**
     * create the create Hearing Response.
     * @return PactJsonBody pact JSON body
     */
    private PactDslJsonBody createCreateHearingResponse() {
        return (PactDslJsonBody) new PactDslJsonBody()
            .stringType("status_message", "Case-User-Role assignments returned successfully")
            .minArrayLike("case_assignments", 1, 1)
            .stringType("case_id", "1588234985453946")
            .minArrayLike("shared_with", 1, 1)
            .stringType("idam_id", "33dff5a7-3b6f-45f1-b5e7-5f9be1ede355")
            .stringType("first_name",  "John")
            .stringType("last_name", "Smith")
            .stringType("email", "John.Smith@gmail.com")
            .minArrayLike("case_roles", 1, PactDslJsonRootValue.stringType("[Collaborator]"), 1)
            .closeArray()
            .closeArray();
    }

    /**
     * create an Invalid Create Hearing Request.
     * @return String JSON body
     */
    private String createInvalidCreateHearingRequest() {
        StringBuilder sb = new StringBuilder();
        return sb.toString();
    }

    /**
     * create a Valid Create Hearing Request.
     * @return String JSON body
     */
    private String createValidCreateHearingRequest() {
        StringBuilder sb = new StringBuilder();
        return sb.toString();
    }

}
