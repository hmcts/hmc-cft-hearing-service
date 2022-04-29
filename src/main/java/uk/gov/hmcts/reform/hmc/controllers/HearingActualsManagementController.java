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

import javax.validation.Valid;

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
        @ApiResponse(code = 400, message = "Invalid hearing Id"),
        @ApiResponse(code = 400, message = "001 No such id: hearingId"),
        @ApiResponse(code = 400, message = "002 invalid status HEARING_REQUESTED"),
        @ApiResponse(code = 400, message = "002 invalid status AWAITING_LISTING"),
        @ApiResponse(code = 400, message = "003 invalid date"),
        @ApiResponse(code = 400, message = "004 non-unique dates"),
        @ApiResponse(code = 500, message = "Error occurred on the server")
    })
    public void updateHearingActuals(@PathVariable("id") Long hearingId,
                                     @RequestBody @Valid HearingActual request) {
        hearingActualsService.updateHearingActuals(hearingId, request);
    }
}
