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
        ReflectionTestUtils.setField(applicationParams, "exponentialMultiplier", VALUE);
        assertEquals(VALUE, applicationParams.getExponentialMultiplier());
    }

    @Test
    void shouldGetInternalConnectionString() {
        ReflectionTestUtils.setField(applicationParams, "internalConnectionString", VALUE);
        assertEquals(VALUE, applicationParams.getInternalConnectionString());
    }

    @Test
    void shouldGetMaxRetryAttempts() {
        ReflectionTestUtils.setField(applicationParams, "maxRetryAttempts", INT_VALUE);
        assertEquals(INT_VALUE, applicationParams.getMaxRetryAttempts());
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
}
