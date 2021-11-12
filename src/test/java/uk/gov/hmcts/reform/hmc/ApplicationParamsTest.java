package uk.gov.hmcts.reform.hmc;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationParamsTest {

    private static final String VALUE = "test-value";
    private final ApplicationParams applicationParams = new ApplicationParams();

    @Test
    void shouldGetTopicName() {
        ReflectionTestUtils.setField(applicationParams, "topicName", VALUE);
        assertEquals(VALUE, applicationParams.getTopicName());
    }

    @Test
    void shouldGetConnectionString() {
        ReflectionTestUtils.setField(applicationParams, "connectionString", VALUE);
        assertEquals(VALUE, applicationParams.getConnectionString());
    }

    @Test
    void shouldGetSubscriptionName() {
        ReflectionTestUtils.setField(applicationParams, "subscriptionName", VALUE);
        assertEquals(VALUE, applicationParams.getSubscriptionName());
    }
}
