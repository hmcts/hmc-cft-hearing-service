package uk.gov.hmcts.reform.hmc.domain.model.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LinkTypeTest {

    @Test
    void isValid() {
        assertTrue(LinkType.isValid("ORDERED"));
    }

    @Test
    void getByLabel() {
        assertEquals(LinkType.ORDERED, LinkType.getByLabel("Ordered"));
    }

    @Test
    void getValue() {
        assertEquals("Ordered", LinkType.ORDERED.label);
    }

}