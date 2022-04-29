package uk.gov.hmcts.reform.hmc.controllers;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
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
        @ApiResponse(code = 200, message = ValidationError.HEARING_ACTUALS_SUCCESSFULLY_PROCESSED),
        @ApiResponse(code = 400,
            message = "One or more of the following reasons:"
            + "\n1) " + ValidationError.HEARING_ACTUALS_NO_HEARING_RESPONSE_FOUND
            + "\n2) " + ValidationError.INVALID_HEARING_ID_DETAILS
            + "\n001) " + ValidationError.HEARING_ACTUALS_ID_NOT_FOUND
            + "\n002) " + ValidationError.HEARING_ACTUALS_INVALID_STATUS
            + "\n003) " + ValidationError.HEARING_ACTUALS_HEARING_DAYS_INVALID
            + "\n004) " + ValidationError.HEARING_ACTUALS_NON_UNIQUE_HEARING_DAYS
        ),
    })
    public void updateHearingActuals(@PathVariable("id") Long hearingId,
                                     @RequestBody @Valid HearingActual request) {
        hearingActualsService.updateHearingActuals(hearingId, request);
    }
}
