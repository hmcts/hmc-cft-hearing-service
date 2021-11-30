package uk.gov.hmcts.reform.hmc.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HearingManagementServiceIT extends BaseTest {

    @Autowired
    private HearingManagementService hearingManagementService;

    private static final String INSERT_CASE_HEARING_DATA_SCRIPT = "classpath:sql/insert-case_hearing_request.sql";

    @Test
    void testValidateHearingRequest() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        hearingManagementService.validateHearingRequest(hearingRequest);
    }

    @Test
    @Sql(INSERT_CASE_HEARING_DATA_SCRIPT)
    void testDeleteHearingRequest_WithAllMandatoryFields() {
        DeleteHearingRequest request = TestingUtil.deleteHearingRequest();
        hearingManagementService.deleteHearingRequest(2000000000L, request);

    }

    @Test
    @Sql(INSERT_CASE_HEARING_DATA_SCRIPT)
    void testDeleteHearingRequest_WithNullHearingId() {
        DeleteHearingRequest request = TestingUtil.deleteHearingRequest();
        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingManagementService.deleteHearingRequest(null, request);
        });
        assertEquals("Invalid hearing Id", exception.getMessage());
    }

    @Test
    @Sql(INSERT_CASE_HEARING_DATA_SCRIPT)
    void testDeleteHearingRequest_WithHearingId_NotPresentInDB() {
        DeleteHearingRequest request = TestingUtil.deleteHearingRequest();
        Exception exception = assertThrows(HearingNotFoundException.class, () -> {
            hearingManagementService.deleteHearingRequest(2000000001L, request);
        });
        assertEquals("No hearing found for reference: 2000000001", exception.getMessage());
    }

    @Test
    @Sql(INSERT_CASE_HEARING_DATA_SCRIPT)
    void testDeleteHearingRequest_WithInvalidHearingIdFormat() {
        DeleteHearingRequest request = TestingUtil.deleteHearingRequest();
        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingManagementService.deleteHearingRequest(300000000L, request);
        });
        assertEquals("Invalid hearing Id", exception.getMessage());
    }
}
