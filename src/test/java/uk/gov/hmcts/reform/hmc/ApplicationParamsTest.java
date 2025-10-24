package uk.gov.hmcts.reform.hmc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.hmc.config.RoleAssignmentUrlManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationParamsTest {

    private static final String VALUE = "test-value";
    private static final int INT_VALUE = 1;

    @Mock
    private RoleAssignmentUrlManager roleAssignmentUrlManager;

    @InjectMocks
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

        when(roleAssignmentUrlManager.getActualHost()).thenReturn(roleAssignmentServiceHost);

        assertEquals(baseUrl, applicationParams.roleAssignmentBaseUrl());
    }

    @Test
    void shouldGetAmGetRoleAssignmentsUrl() {
        final var roleAssignmentServiceHost = "test-value";
        final var baseUrl = roleAssignmentServiceHost + "/am/role-assignments/actors/{uid}";
        when(roleAssignmentUrlManager.getActualHost()).thenReturn(roleAssignmentServiceHost);

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

    @Test
    void shouldGetAuthorisedSupportToolServices() {
        ReflectionTestUtils.setField(applicationParams, "authorisedSupportToolServices", List.of(VALUE));
        assertThat(applicationParams.getAuthorisedSupportToolServices()).isEqualTo(List.of(VALUE));
    }

    @Test
    void shouldGetAuthorisedSupportToolRoles() {
        ReflectionTestUtils.setField(applicationParams, "authorisedSupportToolRoles", List.of(VALUE));
        assertThat(applicationParams.getAuthorisedSupportToolRoles()).isEqualTo(List.of(VALUE));
    }

}
