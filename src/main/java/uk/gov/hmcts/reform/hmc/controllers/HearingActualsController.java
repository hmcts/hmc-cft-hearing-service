package uk.gov.hmcts.reform.hmc.controllers;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.model.hearingactuals.HearingActualResponse;
import uk.gov.hmcts.reform.hmc.service.HearingActualsService;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@Validated
public class HearingActualsController {

    private final HearingActualsService hearingActualsService;

    public HearingActualsController(HearingActualsService hearingActualsService) {
        this.hearingActualsService = hearingActualsService;
    }

    @GetMapping(path = "/hearingActuals/{id}", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Hearing id is valid"),
        @ApiResponse(code = 404, message = ValidationError.HEARING_ID_NOT_FOUND),
        @ApiResponse(code = 400, message = ValidationError.INVALID_HEARING_ID_DETAILS)
    })
    public ResponseEntity<HearingActualResponse> getHearingActuals(@PathVariable("id") Long hearingId) {
        return hearingActualsService.getHearingActuals(hearingId);
    }
}
