package uk.gov.hmcts.reform.hmc.domain.model.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LinkTypeTest {

    @Test
    void isValidName() {
        assertTrue(LinkType.isValidName(LinkType.ORDERED.name()));
        assertTrue(LinkType.isValidName(LinkType.SAME_SLOT.name()));
    }

    @Test
    void isValidLabel() {
        assertTrue(LinkType.isValidLabel("Ordered"));
        assertTrue(LinkType.isValidLabel("Same Slot"));
    }

    @Test
    void getByLabel() {
        assertEquals(LinkType.ORDERED, LinkType.getByLabel("Ordered"));
        assertEquals(LinkType.SAME_SLOT, LinkType.getByLabel("Same Slot"));
    }

    @Test
    void getValue() {
        System.out.println("label " + LinkType.ORDERED.label);
        assertEquals("Ordered", LinkType.ORDERED.label);
        assertEquals("Same Slot", LinkType.SAME_SLOT.label);
    }

}