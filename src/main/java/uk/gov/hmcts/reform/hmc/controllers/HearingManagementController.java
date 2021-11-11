package uk.gov.hmcts.reform.hmc.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.service.HearingManagementService;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class HearingManagementController {

    private final HearingManagementService hearingManagementService;

    public HearingManagementController(HearingManagementService hearingManagementService) {
        this.hearingManagementService = hearingManagementService;
    }

    @PostMapping(path = "/hearing", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ApiResponses(value = {
        @ApiResponse(code = 202, message = "Hearing are valid"),
        @ApiResponse(code = 400, message = "Invalid hearing details found")
    })
    public void invokeHearing(@RequestBody @Valid HearingRequest hearingRequest) {
        hearingManagementService.validateHearingRequest(hearingRequest);
    }

    /**
     * get Case either by caseRefId OR CaseRefId/caseStatus.
     * @param caseRefId case Ref Id
     * @param caseStatus optional Case status
     * @return Hearing
     */
    @Transactional
    @GetMapping(value = {"/hearing/{caseRefId}", "/hearing/{caseRefId}/{caseStatus}"},
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get hearing")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success (with content)"),
        @ApiResponse(code = 204, message = "Success (no content)"),
        @ApiResponse(code = 400, message = "Invalid request"),
        @ApiResponse(code = 401, message = "Unauthorised"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 404, message = "Not found")})
    public HearingRequest getHearingRequest(@PathVariable String caseRefId,
                              @PathVariable(required = false) String caseStatus) {
        return hearingManagementService.validateGetHearingRequest(caseRefId, caseStatus);
    }

}
