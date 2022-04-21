package uk.gov.hmcts.reform.hmc;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationParamsTest {

    private static final String VALUE = "test-value";
    private static final int INT_VALUE = 1;
    private final ApplicationParams applicationParams = new ApplicationParams();

    @Test
    void shouldGetExponentialMultiplier() {
        ReflectionTestUtils.setField(applicationParams, "inboundExponentialMultiplier", VALUE);
        assertEquals(VALUE, applicationParams.getInboundExponentialMultiplier());
    }

    @Test
    void shouldGetInternalConnectionString() {
        ReflectionTestUtils.setField(applicationParams, "internalInboundConnectionString", VALUE);
        assertEquals(VALUE, applicationParams.getInternalInboundConnectionString());
    }

    @Test
    void shouldGetMaxRetryAttempts() {
        ReflectionTestUtils.setField(applicationParams, "inboundMaxRetryAttempts", INT_VALUE);
        assertEquals(INT_VALUE, applicationParams.getInboundMaxRetryAttempts());
    }

    @Test
    void shouldGetExternalTopicName() {
        ReflectionTestUtils.setField(applicationParams, "externalTopicName", VALUE);
        assertEquals(VALUE, applicationParams.getExternalTopicName());
    }

    @Test
    void shouldGetExternalConnectionString() {
        ReflectionTestUtils.setField(applicationParams, "externalConnectionString", VALUE);
        assertEquals(VALUE, applicationParams.getExternalConnectionString());
    }

    @Test
    void shouldGetRoleAssignmentServiceHost() {
        final var roleAssignmentServiceHost = "test-value";
        final var baseUrl = roleAssignmentServiceHost + "/am/role-assignments";

        ReflectionTestUtils.setField(applicationParams, "roleAssignmentServiceHost", roleAssignmentServiceHost);

        assertEquals(baseUrl, applicationParams.roleAssignmentBaseUrl());

    }

    @Test
    void shouldGetAmGetRoleAssignmentsUrl() {
        final var roleAssignmentServiceHost = "test-value";
        final var baseUrl = roleAssignmentServiceHost + "/am/role-assignments/actors/{uid}";

        ReflectionTestUtils.setField(applicationParams, "roleAssignmentServiceHost", roleAssignmentServiceHost);

        assertEquals(baseUrl, applicationParams.amGetRoleAssignmentsUrl());

    }

    @Test
    void shouldGetInternalOutboundConnectionString() {
        ReflectionTestUtils.setField(applicationParams, "internalOutboundConnectionString", VALUE);
        assertEquals(VALUE, applicationParams.getInternalOutboundConnectionString());
    }

    @Test
    void shouldGetInternalOutboundQueueName() {
        ReflectionTestUtils.setField(applicationParams, "internalOutboundQueueName", VALUE);
        assertEquals(VALUE, applicationParams.getInternalOutboundQueueName());
    }

    @Test
    void shouldGetClientId() {
        ReflectionTestUtils.setField(applicationParams, "clientId", VALUE);
        assertEquals(
            VALUE,
            applicationParams.getClientId()
        );
    }

    @Test
    void shouldGetClientSecret() {
        ReflectionTestUtils.setField(applicationParams, "clientSecret", VALUE);
        assertEquals(
            VALUE,
            applicationParams.getClientSecret()
        );
    }

    @Test
    void shouldGetGrantType() {
        ReflectionTestUtils.setField(applicationParams, "grantType", VALUE);
        assertEquals(
            VALUE,
            applicationParams.getGrantType()
        );
    }

    @Test
    void shouldGetScope() {
        ReflectionTestUtils.setField(applicationParams, "scope", VALUE);
        assertEquals(
            VALUE,
            applicationParams.getScope()
        );
    }

    @Test
    void shouldGetSourceSystem() {
        ReflectionTestUtils.setField(applicationParams, "sourceSystem", VALUE);
        assertEquals(
            VALUE,
            applicationParams.getSourceSystem()
        );
    }

    @Test
    void shouldGetDestinationSystem() {
        ReflectionTestUtils.setField(applicationParams, "destinationSystem", VALUE);
        assertEquals(
            VALUE,
            applicationParams.getDestinationSystem()
        );
    }

}
