package uk.gov.hmcts.reform.hmc.controllers;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.model.hearingactuals.HearingActualResponse;
import uk.gov.hmcts.reform.hmc.service.AccessControlService;
import uk.gov.hmcts.reform.hmc.service.HearingActualsService;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.HEARING_VIEWER;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.LISTED_HEARING_VIEWER;

@RestController
@Validated
@RequiredArgsConstructor
public class HearingActualsController {

    private final HearingActualsService hearingActualsService;
    private final AccessControlService accessControlService;

    @GetMapping(path = "/hearingActuals/{id}", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponse(responseCode = "200", description = "Hearing id is valid")
    @ApiResponse(responseCode = "400", description = ValidationError.INVALID_HEARING_ID_DETAILS)
    @ApiResponse(responseCode = "404", description = ValidationError.HEARING_ID_NOT_FOUND)
    public ResponseEntity<HearingActualResponse> getHearingActuals(@PathVariable("id") Long hearingId) {
        accessControlService.verifyHearingCaseAccess(
            hearingId, List.of(HEARING_VIEWER, LISTED_HEARING_VIEWER));

        return hearingActualsService.getHearingActuals(hearingId);
    }
}
