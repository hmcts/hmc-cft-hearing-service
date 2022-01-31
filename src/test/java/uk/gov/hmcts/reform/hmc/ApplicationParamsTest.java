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
    void shouldGetHmiQueueName() {
        ReflectionTestUtils.setField(applicationParams, "hmiQueueName", VALUE);
        assertEquals(VALUE,
                     applicationParams.getHmiQueueName());
    }
}
