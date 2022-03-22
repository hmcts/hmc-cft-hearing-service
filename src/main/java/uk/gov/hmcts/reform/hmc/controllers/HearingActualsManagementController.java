package uk.gov.hmcts.reform.hmc.controllers;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.hmc.model.HearingActual;
import uk.gov.hmcts.reform.hmc.service.HearingActualsService;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class HearingActualsManagementController {

    private final HearingActualsService hearingActualsService;

    public HearingActualsManagementController(HearingActualsService hearingActualsService) {
        this.hearingActualsService = hearingActualsService;
    }

    @PutMapping(path = "/hearingActuals/{id}", consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Hearing actuals update processed"),
        @ApiResponse(code = 400, message = "Invalid hearing details found"),
        @ApiResponse(code = 404, message = "Hearing id not found"),
        @ApiResponse(code = 500, message = "Error occurred on the server")
    })
    public void updateHearingActuals(@PathVariable("id") Long hearingId,
                                     @RequestBody HearingActual request) {
        hearingActualsService.updateHearingActuals(hearingId, request);
    }
}
