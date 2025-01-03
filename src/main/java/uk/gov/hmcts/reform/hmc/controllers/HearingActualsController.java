package uk.gov.hmcts.reform.hmc.controllers;

import com.microsoft.applicationinsights.core.dependencies.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.HEARING_VIEWER;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.LISTED_HEARING_VIEWER;

@RestController
@Validated
public class HearingActualsController {

    private final HearingActualsService hearingActualsService;
    private final AccessControlService accessControlService;

    public HearingActualsController(HearingActualsService hearingActualsService,
            AccessControlService accessControlService) {
        this.hearingActualsService = hearingActualsService;
        this.accessControlService = accessControlService;
    }

    @GetMapping(path = "/hearingActuals/{id}", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponse(responseCode = "204", description = "Hearing id is valid")
    @ApiResponse(responseCode = "404", description = ValidationError.HEARING_ID_NOT_FOUND)
    @ApiResponse(responseCode = "400", description = ValidationError.INVALID_HEARING_ID_DETAILS)

    public ResponseEntity<HearingActualResponse> getHearingActuals(@PathVariable("id") Long hearingId) {
        accessControlService.verifyHearingCaseAccess(hearingId, Lists.newArrayList(
            HEARING_VIEWER,
            LISTED_HEARING_VIEWER));
        return hearingActualsService.getHearingActuals(hearingId);
    }
}
