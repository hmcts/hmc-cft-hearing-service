package uk.gov.hmcts.reform.hmc.service;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.domain.model.enums.ManageRequestStatus;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.InvalidManageHearingServiceException;
import uk.gov.hmcts.reform.hmc.model.ManageExceptionRequest;
import uk.gov.hmcts.reform.hmc.model.ManageExceptionResponse;
import uk.gov.hmcts.reform.hmc.model.SupportRequest;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.hmc.constants.Constants.MANAGE_EXCEPTION_SUCCESS_MESSAGE;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.ADJOURNED;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.AWAITING_LISTING;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.CANCELLATION_SUBMITTED;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.CANCELLED;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.COMPLETED;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.EXCEPTION;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.DUPLICATE_HEARING_IDS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.EMPTY_HEARING_STATE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_NULL;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_CASE_REF_MISMATCH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_FINAL_STATE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_LIMIT;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_STATE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_LAST_GOOD_STATE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_MANAGE_EXCEPTION_ROLE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_MANAGE_HEARING_SERVICE_EXCEPTION;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.LAST_GOOD_STATE_EMPTY;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.convertJsonToRequest;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ManageExceptionsServiceIT extends BaseTest {

    @Autowired
    private ManageExceptionsService manageExceptionsService;

    @Autowired
    private HearingRepository hearingRepository;

    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";
    private static final String INSERT_HEARINGS = "classpath:sql/get-hearings-ManageSupportRequest.sql";
    private static final String INSERT_HEARINGS_NOT_IN_EXCEPTION = "classpath:sql/get-hearings-NotInException.sql";
    private static final String INSERT_HEARINGS_INVALID_ROLLBACK = "classpath:sql/get-hearings-invalid-Rollback.sql";
    private static final String INSERT_HEARINGS_NoActuals = "classpath:sql/get-hearings-No-Actuals.sql";

    ManageExceptionRequest finalStateRequest = convertJsonToRequest(
        "manage-exceptions/valid-final_state_transition_request.json");
    ManageExceptionRequest rollBackRequest = convertJsonToRequest(
        "manage-exceptions/valid-roll_back_request.json");
    ManageExceptionRequest finalStateAndRollbackRequest = convertJsonToRequest(
        "manage-exceptions/valid-final-rollback-request.json");
    ManageExceptionRequest finalStateNoActualsRequest = convertJsonToRequest(
        "manage-exceptions/final-state-request-NoActuals.json");

    private static final String CLIENT_S2S_TOKEN = generateDummyS2SToken("tech_admin_ui");

    private static final String SUCCESS_STATUS = ManageRequestStatus.SUCCESSFUL.label;
    private static final String FAILURE_STATUS = ManageRequestStatus.FAILURE.label;

    private static final String ACTOR_ID = "4d96923f-891a-4cb1-863e-9bec412gt567";
    private static final String hearingID1 = "2000000000";
    private static final String hearingID2 = "2000000001";
    private static final String hearingID3 = "2000000002";

    public ManageExceptionsServiceIT() throws IOException {
    }

    @Nested
    @DisplayName("manageExceptions-rollback-Final-State-Transition")
    class ManageExceptionsRollBackAndFinalStateTransition {

        @Test
        void testManageExceptions_InvalidService_ThrowsException() {
            String invalidToken = generateDummyS2SToken("invalid_service");
            Exception exception = assertThrows(
                InvalidManageHearingServiceException.class,
                () -> manageExceptionsService.manageExceptions(finalStateAndRollbackRequest, invalidToken)
            );
            assertEquals(INVALID_MANAGE_HEARING_SERVICE_EXCEPTION, exception.getMessage());
        }

        @Test
        void testManageExceptions_ExceedHearingIdLimit_ThrowsException() {
            ManageExceptionRequest overLimitRequest = new ManageExceptionRequest();
            List<SupportRequest> requests = new ArrayList<>();
            for (int i = 0; i < 101; i++) {
                SupportRequest req = new SupportRequest();
                req.setHearingId(String.valueOf(2000000000L + i));
                req.setCaseRef("CASE_REF_" + i);
                req.setAction("FINAL_STATE_TRANSITION");
                req.setState("CANCELLATION_SUBMITTED");
                requests.add(req);
            }
            overLimitRequest.setSupportRequests(requests);

            Exception exception = assertThrows(
                BadRequestException.class,
                () -> manageExceptionsService.manageExceptions(overLimitRequest, CLIENT_S2S_TOKEN)
            );
            assertEquals(INVALID_HEARING_ID_LIMIT, exception.getMessage());
        }

        @Test
        void testManageExceptions_InvalidUserRole() {
            WireMock.stubFor(WireMock.get(urlMatching("/o/userinfo"))
                                 .willReturn(okJson(jsonBody(ACTOR_ID))));

            Exception exception = assertThrows(
                InvalidManageHearingServiceException.class,
                () -> manageExceptionsService.manageExceptions(finalStateAndRollbackRequest, CLIENT_S2S_TOKEN)
            );
            assertEquals(INVALID_MANAGE_EXCEPTION_ROLE, exception.getMessage());
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARINGS})
        void testManageExceptions_DuplicateHearingIds_ThrowsException() throws IOException {
            ManageExceptionRequest duplicateRequest = convertJsonToRequest(
                "manage-exceptions/duplicate-hearingIds.json");
            Exception exception = assertThrows(
                BadRequestException.class,
                () -> manageExceptionsService.manageExceptions(duplicateRequest, CLIENT_S2S_TOKEN)
            );
            assertEquals(DUPLICATE_HEARING_IDS, exception.getMessage());
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARINGS})
        void testManageExceptions_CaseRefMismatch() throws IOException {
            ManageExceptionRequest caseRefMismatchRequest = convertJsonToRequest(
                "manage-exceptions/case-ref-mismatch-request.json");
            ManageExceptionResponse response = manageExceptionsService.manageExceptions(caseRefMismatchRequest,
                                                                                        CLIENT_S2S_TOKEN);
            assertEquals(3, response.getSupportRequestResponse().size());
            assertSupportRequestResponse(response, 0, hearingID1, SUCCESS_STATUS,
                                         createExpectedMessage(hearingID1, EXCEPTION.name(),
                                                               CANCELLATION_SUBMITTED.toString()));
            assertSupportRequestResponse(response, 1, hearingID2, SUCCESS_STATUS,
                                         createExpectedMessage(hearingID2, EXCEPTION.name(),
                                                               COMPLETED.toString()));
            assertSupportRequestResponse(response, 2, hearingID3, FAILURE_STATUS,
                                         HEARING_ID_CASE_REF_MISMATCH);
            validateHearingEntityDetails(hearingID2, COMPLETED.toString());
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARINGS})
        void testValidateHearingRequest_WithAllMandatoryFields() {

            ManageExceptionResponse response = manageExceptionsService.manageExceptions(finalStateAndRollbackRequest,
                                                                                        CLIENT_S2S_TOKEN);

            assertEquals(3, response.getSupportRequestResponse().size());
            assertSupportRequestResponse(
                response, 0, hearingID1, SUCCESS_STATUS,
                createExpectedMessage(
                    response.getSupportRequestResponse().get(0).getHearingId(), EXCEPTION.name(),
                    CANCELLATION_SUBMITTED.toString()
                )
            );
            validateHearingEntityDetails(hearingID1, CANCELLATION_SUBMITTED.toString());
            assertSupportRequestResponse(
                response, 1, hearingID2, SUCCESS_STATUS,
                createExpectedMessage(
                    response.getSupportRequestResponse().get(1).getHearingId(), EXCEPTION.name(),
                    finalStateAndRollbackRequest.getSupportRequests().get(1).getState()
                )
            );
            validateHearingEntityDetails(hearingID2,
                                         finalStateAndRollbackRequest.getSupportRequests().get(1).getState());
            assertSupportRequestResponse(
                response, 2, hearingID3, SUCCESS_STATUS,
                createExpectedMessage(
                    response.getSupportRequestResponse().get(2).getHearingId(), EXCEPTION.name(),
                    finalStateAndRollbackRequest.getSupportRequests().get(2).getState()
                ));
            validateHearingEntityDetails(hearingID3,
                                         finalStateAndRollbackRequest.getSupportRequests().get(2).getState());

        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARINGS_NOT_IN_EXCEPTION})
        void testValidateHearing_Not_In_ExceptionState() throws IOException {
            ManageExceptionResponse response = manageExceptionsService.manageExceptions(finalStateAndRollbackRequest,
                                                                                        CLIENT_S2S_TOKEN);
            assertEquals(3, response.getSupportRequestResponse().size());
            assertSupportRequestResponse(response, 0, hearingID1, FAILURE_STATUS,
                                         LAST_GOOD_STATE_EMPTY);
            assertSupportRequestResponse(response, 1, hearingID2, FAILURE_STATUS,
                                         INVALID_HEARING_STATE);
            assertSupportRequestResponse(response, 2, hearingID3, FAILURE_STATUS,
                                         INVALID_HEARING_ID);
        }
    }

    private void validateHearingEntityDetails(String hearingID2, String status) {
        Optional<HearingEntity> hearingEntity = hearingRepository.findById(Long.valueOf(hearingID2));
        assertEquals(status, hearingEntity.get().getStatus());
        assertNull(hearingEntity.get().getErrorCode());
        assertNull(hearingEntity.get().getErrorDescription());
    }


    @Nested
    @DisplayName("manageExceptions-Final-State-Transition")
    class ManageExceptionsFinalStateTransition {

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARINGS})
        void testManageExceptions_InvalidFinalState() throws IOException {
            ManageExceptionRequest invalidFinalState = convertJsonToRequest(
                "manage-exceptions/inValid-final_state_transition_request.json");
            ManageExceptionResponse response = manageExceptionsService.manageExceptions(invalidFinalState,
                                                                                        CLIENT_S2S_TOKEN);
            assertEquals(3, response.getSupportRequestResponse().size());
            assertSupportRequestResponse(response, 0, hearingID1, SUCCESS_STATUS,
                                         createExpectedMessage(hearingID1, EXCEPTION.name(),
                                         finalStateRequest.getSupportRequests().get(0).getState()));
            assertSupportRequestResponse(response, 1, hearingID2, FAILURE_STATUS,
                                         INVALID_HEARING_ID_FINAL_STATE);
            assertSupportRequestResponse(response, 2, hearingID3, FAILURE_STATUS,
                                         HEARING_ID_CASE_REF_MISMATCH);
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARINGS})
        void testManageExceptions_ValidFinalState() throws IOException {
            ManageExceptionResponse response = manageExceptionsService.manageExceptions(finalStateRequest,
                                                                                        CLIENT_S2S_TOKEN);
            assertEquals(3, response.getSupportRequestResponse().size());
            assertSupportRequestResponse(response, 0, hearingID1, SUCCESS_STATUS,
                                         createExpectedMessage(hearingID1, EXCEPTION.name(),
                                          finalStateRequest.getSupportRequests().get(0).getState()));
            assertSupportRequestResponse(response, 1, hearingID2, SUCCESS_STATUS,
                                         createExpectedMessage(hearingID2, EXCEPTION.name(),
                                                               COMPLETED.toString()));
            assertSupportRequestResponse(response, 2, hearingID3, SUCCESS_STATUS,
                                         createExpectedMessage(hearingID3, EXCEPTION.name(),
                                                               ADJOURNED.toString()));
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARINGS})
        void testManageExceptions_NoState() throws IOException {
            finalStateRequest.getSupportRequests().get(0).setState(null);
            ManageExceptionResponse response = manageExceptionsService.manageExceptions(finalStateRequest,
                                                                                        CLIENT_S2S_TOKEN);
            assertEquals(3, response.getSupportRequestResponse().size());
            assertSupportRequestResponse(response, 0, hearingID1, FAILURE_STATUS, EMPTY_HEARING_STATE);
            assertSupportRequestResponse(response, 1, hearingID2, SUCCESS_STATUS,
                                         createExpectedMessage(hearingID2, EXCEPTION.name(),
                                                               COMPLETED.toString()));
            assertSupportRequestResponse(response, 2, hearingID3, SUCCESS_STATUS,
                                         createExpectedMessage(hearingID3, EXCEPTION.name(),
                                                               ADJOURNED.toString()));
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARINGS_NoActuals})
        void testManageExceptions_NoActuals() throws IOException {
            ManageExceptionResponse response = manageExceptionsService.manageExceptions(finalStateNoActualsRequest,
                                                                                        CLIENT_S2S_TOKEN);
            assertEquals(3, response.getSupportRequestResponse().size());
            assertSupportRequestResponse(response, 0, hearingID1, SUCCESS_STATUS,
                                         createExpectedMessage(hearingID1, EXCEPTION.name(),
                                                               CANCELLED.toString()));
            assertSupportRequestResponse(response, 1, hearingID2, SUCCESS_STATUS,
                                         createExpectedMessage(hearingID2, EXCEPTION.name(),
                                                               COMPLETED.toString()));
            assertSupportRequestResponse(response, 2, hearingID3, FAILURE_STATUS, HEARING_ACTUALS_NULL);
        }
    }

    @Nested
    @DisplayName("manageExceptions-rollback")
    class ManageExceptionsRollback {

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARINGS_INVALID_ROLLBACK})
        void testManageExceptions_InvalidRollback() throws IOException {
            ManageExceptionRequest invalidRollback = convertJsonToRequest(
                "manage-exceptions/inValid-roll_back_request.json");
            ManageExceptionResponse response = manageExceptionsService.manageExceptions(invalidRollback,
                                                                                        CLIENT_S2S_TOKEN);
            assertEquals(3, response.getSupportRequestResponse().size());
            assertSupportRequestResponse(response, 0, hearingID1, FAILURE_STATUS,
                                         LAST_GOOD_STATE_EMPTY);
            assertSupportRequestResponse(response, 1, hearingID2, FAILURE_STATUS,
                                         INVALID_HEARING_STATE);
            assertSupportRequestResponse(response, 2, hearingID3, FAILURE_STATUS,
                                         INVALID_LAST_GOOD_STATE);
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARINGS})
        void testManageExceptions_ValidRollback() throws IOException {
            ManageExceptionResponse response = manageExceptionsService.manageExceptions(rollBackRequest,
                                                                                        CLIENT_S2S_TOKEN);
            assertEquals(3, response.getSupportRequestResponse().size());
            assertSupportRequestResponse(response, 0, hearingID1, SUCCESS_STATUS,
                                         createExpectedMessage(hearingID1, EXCEPTION.name(),
                                                               CANCELLATION_SUBMITTED.toString()));
            assertSupportRequestResponse(response, 1, hearingID2, SUCCESS_STATUS,
                                         createExpectedMessage(hearingID2, EXCEPTION.name(),
                                                               AWAITING_LISTING.toString()));
            assertSupportRequestResponse(response, 2, hearingID3, SUCCESS_STATUS,
                                         createExpectedMessage(hearingID3, EXCEPTION.name(),
                                                               CANCELLATION_SUBMITTED.toString()));
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

    public static String jsonBody(String id) {
        return "{\n"
            + "      \"sub\": \"" + id + "\",\n"
            + "      \"uid\": \"" + id + "\",\n"
            + "      \"name\": \"Test User\",\n"
            + "      \"given_name\": \"Test\",\n"
            + "      \"family_name\": \"User\",\n"
            + "      \"roles\": [\"caseworker-test\"]\n"
            + "}";

    }

}
