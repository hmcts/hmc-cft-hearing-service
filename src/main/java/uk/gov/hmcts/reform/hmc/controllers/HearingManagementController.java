package uk.gov.hmcts.reform.hmc.controllers;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
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
        @ApiResponse(code = 400, message = "Invalid hearing details found")
    })
    public void invokeHearing(@RequestBody @Valid HearingRequest hearingRequest) {
        hearingManagementService.validateHearingRequest(hearingRequest);
    }
}
