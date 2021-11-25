package uk.gov.hmcts.reform.hmc;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationParamsTest {

    private static final String VALUE = "test-value";
    private final ApplicationParams applicationParams = new ApplicationParams();

    @Test
    void shouldGetInboundTopicName() {
        ReflectionTestUtils.setField(applicationParams, "inboundTopicName", VALUE);
        assertEquals(VALUE, applicationParams.getInboundTopicName());
    }

    @Test
    void shouldGetInboundConnectionString() {
        ReflectionTestUtils.setField(applicationParams, "inboundConnectionString", VALUE);
        assertEquals(VALUE, applicationParams.getInboundConnectionString());
    }

    @Test
    void shouldGetInboundSubscriptionName() {
        ReflectionTestUtils.setField(applicationParams, "inboundSubscriptionName", VALUE);
        assertEquals(VALUE, applicationParams.getInboundSubscriptionName());
    }

    @Test
    void shouldGetOutboundTopicName() {
        ReflectionTestUtils.setField(applicationParams, "outboundTopicName", VALUE);
        assertEquals(VALUE, applicationParams.getOutboundTopicName());
    }

    @Test
    void shouldGetOutboundConnectionString() {
        ReflectionTestUtils.setField(applicationParams, "outboundConnectionString", VALUE);
        assertEquals(VALUE, applicationParams.getOutboundConnectionString());
    }
}
