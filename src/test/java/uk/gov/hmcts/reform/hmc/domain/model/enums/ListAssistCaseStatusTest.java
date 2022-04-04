package uk.gov.hmcts.reform.hmc.domain.model.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListAssistCaseStatusTest {

    @Test
    void isValidName() {
        assertTrue(ListAssistCaseStatus.isValidName(ListAssistCaseStatus.AWAITING_LISTING.name()));
        assertTrue(ListAssistCaseStatus.isValidName(ListAssistCaseStatus.CASE_CLOSED.name()));
        assertTrue(ListAssistCaseStatus.isValidName(ListAssistCaseStatus.CASE_CREATED.name()));
        assertTrue(ListAssistCaseStatus.isValidName(ListAssistCaseStatus.HEARING_COMPLETED.name()));
        assertTrue(ListAssistCaseStatus.isValidName(ListAssistCaseStatus.LISTED.name()));
        assertTrue(ListAssistCaseStatus.isValidName(ListAssistCaseStatus.PENDING_RELISTING.name()));
    }

    @Test
    void isValidLabel() {
        assertTrue(ListAssistCaseStatus.isValidLabel("Awaiting Listing"));
        assertTrue(ListAssistCaseStatus.isValidLabel("Case Closed"));
        assertTrue(ListAssistCaseStatus.isValidLabel("Case Created"));
        assertTrue(ListAssistCaseStatus.isValidLabel("Hearing Completed"));
        assertTrue(ListAssistCaseStatus.isValidLabel("Listed"));
        assertTrue(ListAssistCaseStatus.isValidLabel("Pending Relisting"));
    }

    @Test
    void getByLabel() {
        assertEquals(ListAssistCaseStatus.AWAITING_LISTING, ListAssistCaseStatus.getByLabel("Awaiting Listing"));
        assertEquals(ListAssistCaseStatus.CASE_CLOSED, ListAssistCaseStatus.getByLabel("Case Closed"));
        assertEquals(ListAssistCaseStatus.CASE_CREATED, ListAssistCaseStatus.getByLabel("Case Created"));
        assertEquals(ListAssistCaseStatus.HEARING_COMPLETED, ListAssistCaseStatus.getByLabel("Hearing Completed"));
        assertEquals(ListAssistCaseStatus.LISTED, ListAssistCaseStatus.getByLabel("Listed"));
        assertEquals(ListAssistCaseStatus.PENDING_RELISTING, ListAssistCaseStatus.getByLabel("Pending Relisting"));
    }

    @Test
    void getByName() {
        assertEquals(ListAssistCaseStatus.AWAITING_LISTING, ListAssistCaseStatus.getByName("AWAITING_LISTING"));
        assertEquals(ListAssistCaseStatus.CASE_CLOSED, ListAssistCaseStatus.getByName("CASE_CLOSED"));
        assertEquals(ListAssistCaseStatus.CASE_CREATED, ListAssistCaseStatus.getByName("CASE_CREATED"));
        assertEquals(ListAssistCaseStatus.HEARING_COMPLETED, ListAssistCaseStatus.getByName("HEARING_COMPLETED"));
        assertEquals(ListAssistCaseStatus.LISTED, ListAssistCaseStatus.getByName("LISTED"));
        assertEquals(ListAssistCaseStatus.PENDING_RELISTING, ListAssistCaseStatus.getByName("PENDING_RELISTING"));
    }

    @Test
    void getValue() {
        assertEquals("Awaiting Listing", ListAssistCaseStatus.AWAITING_LISTING.label);
        assertEquals("Case Closed", ListAssistCaseStatus.CASE_CLOSED.label);
        assertEquals("Case Created", ListAssistCaseStatus.CASE_CREATED.label);
        assertEquals("Hearing Completed", ListAssistCaseStatus.HEARING_COMPLETED.label);
        assertEquals("Listed", ListAssistCaseStatus.LISTED.label);
        assertEquals("Pending Relisting", ListAssistCaseStatus.PENDING_RELISTING.label);
    }

    @Test
    void getLabelFromName() {
        assertEquals("Awaiting Listing", ListAssistCaseStatus.getLabel("AWAITING_LISTING"));
        assertEquals("Case Closed", ListAssistCaseStatus.getLabel("CASE_CLOSED"));
        assertEquals("Case Created", ListAssistCaseStatus.getLabel("CASE_CREATED"));
        assertEquals("Hearing Completed", ListAssistCaseStatus.getLabel("HEARING_COMPLETED"));
        assertEquals("Listed", ListAssistCaseStatus.getLabel("LISTED"));
        assertEquals("Pending Relisting", ListAssistCaseStatus.getLabel("PENDING_RELISTING"));
    }

}