package uk.gov.hmcts.reform.hmc.domain.model.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListingStatusTest {

    @Test
    void isValidName() {
        assertTrue(ListingStatus.isValidName(ListingStatus.DRAFT.name()));
        assertTrue(ListingStatus.isValidName(ListingStatus.FIXED.name()));
        assertTrue(ListingStatus.isValidName(ListingStatus.PROVISIONAL.name()));
    }

    @Test
    void isValidLabel() {
        assertTrue(ListingStatus.isValidLabel("Draft"));
        assertTrue(ListingStatus.isValidLabel("Fixed"));
        assertTrue(ListingStatus.isValidLabel("Provisional"));
    }

    @Test
    void getByLabel() {
        assertEquals(ListingStatus.DRAFT, ListingStatus.getByLabel("Draft"));
        assertEquals(ListingStatus.FIXED, ListingStatus.getByLabel("Fixed"));
        assertEquals(ListingStatus.PROVISIONAL, ListingStatus.getByLabel("Provisional"));
    }

    @Test
    void getByName() {
        assertEquals(ListingStatus.DRAFT, ListingStatus.getByName("DRAFT"));
        assertEquals(ListingStatus.FIXED, ListingStatus.getByName("FIXED"));
        assertEquals(ListingStatus.PROVISIONAL, ListingStatus.getByName("PROVISIONAL"));
    }

    @Test
    void getValue() {
        assertEquals("Draft", ListingStatus.DRAFT.label);
        assertEquals("Fixed", ListingStatus.FIXED.label);
        assertEquals("Provisional", ListingStatus.PROVISIONAL.label);
    }

    @Test
    void getLabelFromName() {
        assertEquals("Draft", ListingStatus.getLabel("DRAFT"));
        assertEquals("Fixed", ListingStatus.getLabel("FIXED"));
        assertEquals("Provisional", ListingStatus.getLabel("PROVISIONAL"));
    }
}