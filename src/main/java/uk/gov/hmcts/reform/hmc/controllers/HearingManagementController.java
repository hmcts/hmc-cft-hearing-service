package uk.gov.hmcts.reform.hmc.controllers;

import com.microsoft.applicationinsights.core.dependencies.google.common.collect.Lists;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.hibernate.validator.constraints.LuhnCheck;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.GetHearingResponse;
import uk.gov.hmcts.reform.hmc.model.GetHearingsResponse;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.UpdateHearingRequest;
import uk.gov.hmcts.reform.hmc.service.AccessControlService;
import uk.gov.hmcts.reform.hmc.service.HearingManagementService;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.AMEND_HEARING;
import static uk.gov.hmcts.reform.hmc.constants.Constants.DELETE_HEARING;
import static uk.gov.hmcts.reform.hmc.constants.Constants.REQUEST_HEARING;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_REF_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_REF_INVALID;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_REF_INVALID_LENGTH;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.HEARNING_MANAGER;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.HEARNING_VIEWER;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.LISTED_HEARING_VIEWER;

@RestController
@Validated
public class HearingManagementController {

    private final HearingManagementService hearingManagementService;
    private final AccessControlService accessControlService;

    public HearingManagementController(HearingManagementService hearingManagementService,
                                       AccessControlService accessControlService) {
        this.hearingManagementService = hearingManagementService;
        this.accessControlService = accessControlService;
    }

    @GetMapping(path = "/hearing/{id}", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Hearing id is valid"),
        @ApiResponse(code = 404, message = "Hearing id not found"),
        @ApiResponse(code = 400, message = "Invalid hearing id")
    })
    public ResponseEntity<GetHearingResponse> getHearing(@PathVariable("id") Long hearingId,
                                                         @RequestParam(value = "isValid",
                                                             defaultValue = "false") boolean isValid) {
        accessControlService.verifyHearingCaseAccess(hearingId, Lists.newArrayList(HEARNING_MANAGER,
                                                                                   LISTED_HEARING_VIEWER));
        return hearingManagementService.getHearingRequest(hearingId, isValid);
    }

    @PostMapping(path = "/hearing", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Hearing Id is created"),
        @ApiResponse(code = 400, message = "Invalid hearing details found")
    })
    public HearingResponse saveHearing(@RequestBody @Valid HearingRequest createHearingRequest) {
        accessControlService.verifyCaseAccess(getCaseRef(createHearingRequest), Lists.newArrayList(HEARNING_MANAGER));
        HearingResponse hearingResponse = hearingManagementService.saveHearingRequest(createHearingRequest);
        hearingManagementService.sendRequestToHmiAndQueue(hearingResponse.getHearingRequestId(), createHearingRequest,
                REQUEST_HEARING
        );
        return hearingResponse;
    }

    @DeleteMapping(path = "/hearing/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Hearing cancellation processed"),
        @ApiResponse(code = 400, message = "Invalid hearing details found"),
        @ApiResponse(code = 404, message = "Hearing id not found"),
        @ApiResponse(code = 500, message = "Error occurred on the server")
    })
    public HearingResponse deleteHearing(@PathVariable("id") Long hearingId,
                                         @RequestBody @Valid DeleteHearingRequest deleteRequest) {
        accessControlService.verifyHearingCaseAccess(hearingId, Lists.newArrayList(HEARNING_MANAGER));
        HearingResponse hearingResponse = hearingManagementService.deleteHearingRequest(
            hearingId, deleteRequest);
        hearingManagementService.sendRequestToHmiAndQueue(deleteRequest, hearingId, DELETE_HEARING);

        return hearingResponse;
    }

    /**
     * get Case either by caseRefId OR CaseRefId/caseStatus.
     * @param ccdCaseRef case Ref
     * @param status optional Status
     * @return Hearing
     */
    @Transactional
    @GetMapping(value = {"/hearings/{ccdCaseRef}"},
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Get hearings")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Success (with content)"),
        @ApiResponse(code = 400, message = "Invalid request")
    })
    public GetHearingsResponse getHearings(@PathVariable("ccdCaseRef")
                                           @Valid
                                           @NotEmpty(message = CASE_REF_EMPTY)
                                           @Size(min = 16, max = 16, message = CASE_REF_INVALID_LENGTH)
                                           @LuhnCheck(message = CASE_REF_INVALID, ignoreNonDigitCharacters = false)
                                               String ccdCaseRef,
                                           @RequestParam(required = false)
                                               String status) {
        if ("LISTED".equals(status)) {
            accessControlService.verifyCaseAccess(ccdCaseRef, Lists.newArrayList(HEARNING_VIEWER,
                                                                                 LISTED_HEARING_VIEWER));
        } else {
            accessControlService.verifyCaseAccess(ccdCaseRef, Lists.newArrayList(HEARNING_VIEWER));
        }
        return hearingManagementService.getHearings(ccdCaseRef, status);
    }

    @PutMapping(path = "/hearing/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Hearing successfully updated"),
        @ApiResponse(code = 400, message = "Invalid hearing details found"),
        @ApiResponse(code = 404, message = "Hearing id not found"),
        @ApiResponse(code = 500, message = "Error occurred on the server")
    })
    public HearingResponse updateHearing(@RequestBody @Valid UpdateHearingRequest hearingRequest,
                                         @PathVariable("id") Long hearingId) {
        accessControlService.verifyHearingCaseAccess(hearingId, Lists.newArrayList(HEARNING_MANAGER));
        HearingResponse hearingResponse = hearingManagementService.updateHearingRequest(hearingId, hearingRequest);
        hearingManagementService.sendRequestToHmiAndQueue(hearingId, hearingRequest, AMEND_HEARING);
        return hearingResponse;
    }

    @PostMapping(path = "/hearingActualsCompletion/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success (with no content)"),
        @ApiResponse(code = 404, message = "001 No such id"),
        @ApiResponse(code = 400, message = "Invalid hearing details found"),
        @ApiResponse(code = 400, message = "002 invalid status"),
        @ApiResponse(code = 400, message = "003 missing hearing day actuals"),
        @ApiResponse(code = 400, message = "004 unexpected hearing day actuals"),
        @ApiResponse(code = 400, message = "005 missing hearing outcome"),
        @ApiResponse(code = 500, message = "Error occurred on the server")
    })
    public ResponseEntity hearingCompletion(@PathVariable("id") Long hearingId) {
        accessControlService.verifyHearingCaseAccess(hearingId, Lists.newArrayList(HEARNING_MANAGER));
        return hearingManagementService.hearingCompletion(hearingId);
    }

    private String getCaseRef(HearingRequest hearingRequest) {
        if (null == hearingRequest || null == hearingRequest.getCaseDetails()) {
            return null;
        }
        return hearingRequest.getCaseDetails().getCaseRef();
    }
}
