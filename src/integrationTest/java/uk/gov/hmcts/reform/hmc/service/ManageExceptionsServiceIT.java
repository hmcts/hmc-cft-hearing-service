package uk.gov.hmcts.reform.hmc.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.domain.model.enums.ManageRequestStatus;
import uk.gov.hmcts.reform.hmc.model.ManageExceptionRequest;
import uk.gov.hmcts.reform.hmc.model.ManageExceptionResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.hmc.constants.Constants.MANAGE_EXCEPTION_SUCCESS_MESSAGE;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.EXCEPTION;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.convertJsonToRequest;

public class ManageExceptionsServiceIT extends BaseTest {

    @Autowired
    private ManageExceptionsService manageExceptionsService;

    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";
    private static final String INSERT_HEARINGS = "classpath:sql/get-hearings-ManageSupportRequest.sql";

    ManageExceptionRequest finalStateRequest = convertJsonToRequest(
        "manage-exceptions/valid-final_state_transition_request.json");
    ManageExceptionRequest rollBackRequest = convertJsonToRequest(
        "manage-exceptions/valid-roll_back_request.json");
    ManageExceptionRequest finalStateAndRollbackRequest = convertJsonToRequest(
        "manage-exceptions/valid-final-rollback-request.json");

    private final String CLIENT_S2S_TOKEN = generateDummyS2SToken("tech_admin_ui");

    private static final String SUCCESS_STATUS = ManageRequestStatus.SUCCESSFUL.label;
    private static final String FAILURE_STATUS = ManageRequestStatus.FAILURE.label;

    public ManageExceptionsServiceIT() throws IOException {
    }

    @Nested
    @DisplayName("manageExceptions-rollback-Final State Transition")
    class ManageExceptionsRollBackAndFinalStateTransition {

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARINGS})
        void testValidateHearingRequest_WithAllMandatoryFields() {

            ManageExceptionResponse response = manageExceptionsService.manageExceptions(finalStateAndRollbackRequest,
                                                                                        CLIENT_S2S_TOKEN);

            assertEquals(3, response.getSupportRequestResponse().size());
            assertSupportRequestResponse(response, 0, "2000000000", SUCCESS_STATUS,
                                         createExpectedMessage(response.getSupportRequestResponse().get(0).getHearingId(), EXCEPTION.name(),
                                                               finalStateAndRollbackRequest.getSupportRequests().get(0).getState()));
            assertSupportRequestResponse(response, 1, "2000000001", SUCCESS_STATUS,
                                         createExpectedMessage(response.getSupportRequestResponse().get(1).getHearingId(), EXCEPTION.name(),
                                                               finalStateAndRollbackRequest.getSupportRequests().get(1).getState()));
            assertSupportRequestResponse(response, 2, "2000000002", SUCCESS_STATUS,
                                         createExpectedMessage(response.getSupportRequestResponse().get(2).getHearingId(), EXCEPTION.name(),
                                                               finalStateAndRollbackRequest.getSupportRequests().get(2).getState()));

        }
    }

    private void assertSupportRequestResponse(ManageExceptionResponse response, int index,
                                              String hearingId, String status, String message) {
        assertEquals(hearingId, response.getSupportRequestResponse().get(index).getHearingId());
        assertEquals(status, response.getSupportRequestResponse().get(index).getStatus());
        assertEquals(message, response.getSupportRequestResponse().get(index).getMessage());
    }

    private String createExpectedMessage(String hearingId, String oldStatus, String newStatus) {
        return String.format(
            MANAGE_EXCEPTION_SUCCESS_MESSAGE,
            hearingId,
            oldStatus,
            newStatus);
    }

}
