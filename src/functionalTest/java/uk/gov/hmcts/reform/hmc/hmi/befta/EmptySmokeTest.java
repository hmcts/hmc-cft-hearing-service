package uk.gov.hmcts.reform.hmc.hmi.befta;

import org.junit.Assert;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class EmptySmokeTest {

    @Tag("smoke")
    @Test
    void shouldRetrieveWhenExists() {
        Assert.assertTrue(true);
        Assert.assertFalse(false);    }

}
