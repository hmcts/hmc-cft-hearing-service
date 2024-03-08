package uk.gov.hmcts.reform.hmc.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.hmc.BaseTest;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.protocol.HTTP.CONTENT_TYPE;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpMethod.PUT;
import static wiremock.org.apache.http.entity.ContentType.APPLICATION_JSON;

@TestPropertySource(properties = {
    "http.client.connection.timeout=1500",
    "http.client.max.total=1",
    "http.client.read.timeout=1500",
    "http.client.seconds.idle.connection=1",
    "http.client.max.client_per_route=2",
    "http.client.validate.after.inactivity=1"
})
public class RestTemplateConfigurationTest extends BaseTest {

    private String getBaseUrl() {
        return "http://localhost:" + wiremockPort;
    }

    private static final JsonNode RESPONSE_BODY = new ObjectMapper().createObjectNode().put("test", "name");
    private static final String URL = "/ng/itb";
    private static final String MIME_TYPE = APPLICATION_JSON.getMimeType();

    private RestTemplate restTemplate;
    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        executorService = Executors.newFixedThreadPool(25);
    }

    @AfterEach
    void tearDown() {
        executorService.shutdown();
    }

    @Test
    public void restTemplateShouldBeUsable() {
        stubResponse();

        final RequestEntity<String> request = new RequestEntity<>(PUT, URI.create(getBaseUrl() + URL));
        final ResponseEntity<JsonNode> response = restTemplate.exchange(request, JsonNode.class);
        assertResponse(response);
    }

    @Disabled()
    @Test
    public void shouldTimeOut() {
        WireMock.stubFor(get(urlEqualTo(URL)).willReturn(aResponse().withStatus(SC_OK).withFixedDelay(2000)));
        RestTemplate restTemplate = new RestTemplate();
        final RequestEntity<Void> request = RequestEntity.get(URI.create("http://localhost:" + wiremockPort + URL)).build();
        assertThrows(ResourceAccessException.class, () -> restTemplate.exchange(request, String.class));
    }

    @Disabled("for local dev only")
    @Test
    public void shouldBeAbleToUseMultipleTimes() throws Exception {
        stubResponse();
        final List<Future<Integer>> futures = new ArrayList<>();
        final int totalNumberOfCalls = 200;

        for (int i = 0; i < totalNumberOfCalls; i++) {
            futures.add(executorService.submit(() -> {
                final RequestEntity<String> request = new RequestEntity<>(PUT, URI.create(getBaseUrl() + URL));
                final ResponseEntity<JsonNode> response = restTemplate.exchange(request, JsonNode.class);
                assertResponse(response);
                return response.getStatusCode().value();
            }));
        }

        MatcherAssert.assertThat(futures, hasSize(totalNumberOfCalls));

        for (Future<Integer> future : futures) {
            MatcherAssert.assertThat(future.get(), is(SC_OK));
        }
    }

    private void stubResponse() {
        WireMock.stubFor(put(urlEqualTo(URL))
                             .willReturn(aResponse().withStatus(SC_OK)
                                             .withHeader(CONTENT_TYPE, MIME_TYPE)
                                             .withBody(RESPONSE_BODY.toString())));
    }

    private void assertResponse(final ResponseEntity<JsonNode> response) {
        ObjectMapper objectMapper = new ObjectMapper();

        MatcherAssert.assertThat(response.getBody(), is(objectMapper.convertValue(RESPONSE_BODY, JsonNode.class)));
        MatcherAssert.assertThat(response.getHeaders().get(CONTENT_TYPE), contains(MIME_TYPE));
        MatcherAssert.assertThat(response.getStatusCode().value(), is(SC_OK));
    }
}
