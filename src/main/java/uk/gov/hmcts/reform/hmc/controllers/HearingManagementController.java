package uk.gov.hmcts.reform.hmc.controllers;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
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
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.UpdateHearingRequest;
import uk.gov.hmcts.reform.hmc.service.HearingManagementService;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
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
    public HearingResponse saveHearing(@RequestBody @Valid HearingRequest hearingRequest) {
        hearingManagementService.verifyAccess(hearingRequest.getCaseDetails().getCaseRef());
        HearingResponse hearingResponse = hearingManagementService.saveHearingRequest(hearingRequest);
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
    public void deleteHearing(@PathVariable("id") Long hearingId,
                              @RequestBody @Valid DeleteHearingRequest deleteRequest) {
        hearingManagementService.deleteHearingRequest(hearingId, deleteRequest);
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
    }
}
