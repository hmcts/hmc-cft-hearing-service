package uk.gov.hmcts.reform.hmc.client.futurehearing;

import feign.Retryer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ActiveDirectoryApiClientConfigTest {

    private static final long PERIOD = 100L;
    private static final long MAX_PERIOD = 1000L;
    private static final int MAX_ATTEMPTS = 3;

    private ActiveDirectoryApiClientConfig activeDirectoryApiClientConfig;

    @BeforeEach
    void setUp() {
        activeDirectoryApiClientConfig = new ActiveDirectoryApiClientConfig();
    }

    @Test
    void shouldCreateActiveDirectoryRetryer() {
        Retryer activeDirectoryRetryer =
            activeDirectoryApiClientConfig.activeDirectoryRetryer(PERIOD, MAX_PERIOD, MAX_ATTEMPTS);

        assertNotNull(activeDirectoryRetryer, "Active Directory Retryer should be created");
    }
}
