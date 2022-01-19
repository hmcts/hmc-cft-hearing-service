package uk.gov.hmcts.reform.hmc.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.hibernate.validator.constraints.LuhnCheck;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import uk.gov.hmcts.reform.hmc.model.CreateHearingRequest;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.UpdateHearingRequest;
import uk.gov.hmcts.reform.hmc.model.hmi.HmiSubmitHearingRequest;
import uk.gov.hmcts.reform.hmc.service.HearingManagementService;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_REF_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_REF_INVALID;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_REF_INVALID_LENGTH;

@RestController
@Validated
public class HearingManagementController {

    private final HearingManagementService hearingManagementService;

    public HearingManagementController(HearingManagementService hearingManagementService) {
        this.hearingManagementService = hearingManagementService;
    }

    @GetMapping(path = "/hearing/{id}", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Hearing id is valid"),
        @ApiResponse(code = 404, message = "Invalid hearing id")
    })
    public void getHearing(@PathVariable("id") Long hearingId,
                           @RequestParam(value = "isValid", defaultValue = "false") boolean isValid) {

        hearingManagementService.getHearingRequest(hearingId, isValid);
    }

    @PostMapping(path = "/hearing", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Hearing Id is created"),
        @ApiResponse(code = 400, message = "Invalid hearing details found")
    })
    public HearingResponse saveHearing(@RequestBody @Valid CreateHearingRequest createHearingRequest) {
        hearingManagementService.verifyAccess(createHearingRequest.getCaseDetails().getCaseRef());
        HearingResponse hearingResponse = hearingManagementService.saveHearingRequest(createHearingRequest);
        hearingManagementService.sendRequestToHmi(hearingResponse.getHearingRequestId(), createHearingRequest);
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
        HearingResponse hearingResponse =  hearingManagementService.deleteHearingRequest(hearingId, deleteRequest);
        return hearingResponse;
    }

    /**
     * get Case either by caseRefId OR CaseRefId/caseStatus.
     *
     * @param ccdCaseRef case Ref
     * @param status     optional Status
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
    public CreateHearingRequest getHearingsRequest(@PathVariable("ccdCaseRef")
                                             @Valid
                                             @NotEmpty(message = CASE_REF_EMPTY)
                                             @Size(min = 16, max = 16, message = CASE_REF_INVALID_LENGTH)
                                             @LuhnCheck(message = CASE_REF_INVALID, ignoreNonDigitCharacters = false)
                                                 String ccdCaseRef,
                                                   @RequestParam(required = false)
                                                 String status) {
        return hearingManagementService.validateGetHearingsRequest(ccdCaseRef, status);
    }

    @PutMapping(path = "/hearing/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Hearing successfully updated"),
        @ApiResponse(code = 400, message = "Invalid hearing details found"),
        @ApiResponse(code = 404, message = "Hearing id not found"),
        @ApiResponse(code = 500, message = "Error occurred on the server")
    })
    public void updateHearing(@RequestBody @Valid UpdateHearingRequest hearingRequest,
                              @PathVariable("id") Long hearingId) {
        hearingManagementService.updateHearingRequest(hearingId, hearingRequest);
        hearingManagementService.sendRequestToHmi(hearingId, hearingRequest);
    }


    @PostMapping(path = "/test", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public HmiSubmitHearingRequest test(@RequestBody @Valid CreateHearingRequest createHearingRequest) {
        hearingManagementService.verifyAccess(createHearingRequest.getCaseDetails().getCaseRef());
        HearingResponse hearingResponse = hearingManagementService.saveHearingRequest(createHearingRequest);
        return hearingManagementService.test(
            hearingResponse.getHearingRequestId(),
            createHearingRequest
        );
    }

    @PutMapping(path = "/test/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public HmiSubmitHearingRequest test(@RequestBody @Valid UpdateHearingRequest hearingRequest,
                                        @PathVariable("id") Long hearingId) {
        hearingManagementService.updateHearingRequest(hearingId, hearingRequest);
        return hearingManagementService.test(hearingId, hearingRequest);
    }
}
