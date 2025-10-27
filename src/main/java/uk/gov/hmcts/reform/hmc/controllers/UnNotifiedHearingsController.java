package uk.gov.hmcts.reform.hmc.controllers;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.model.UnNotifiedHearingsResponse;
import uk.gov.hmcts.reform.hmc.service.UnNotifiedHearingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HMCTS_SERVICE_CODE_EMPTY;

@RestController
@Validated
public class UnNotifiedHearingsController {

    private final UnNotifiedHearingService unNotifiedHearingService;

    public UnNotifiedHearingsController(UnNotifiedHearingService unNotifiedHearingService) {
        this.unNotifiedHearingService = unNotifiedHearingService;
    }

    @GetMapping(path = "/unNotifiedHearings/{hmctsServiceCode}", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "400", description = ValidationError.INVALID_HMCTS_SERVICE_CODE)
    @ApiResponse(responseCode = "401", description = "Unauthorised")
    @ApiResponse(responseCode = "403", description = "Forbidden")

    public UnNotifiedHearingsResponse getUnNotifiedHearings(
        @PathVariable("hmctsServiceCode") @Valid @NotEmpty(message = HMCTS_SERVICE_CODE_EMPTY) String hmctsServiceCode,
        @RequestParam(name = "hearing_start_date_from") 
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") 
            LocalDateTime hearingStartDateFrom,
        @RequestParam(name = "hearing_start_date_to", required = false) 
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") 
            LocalDateTime hearingStartDateTo,
        @RequestParam(required = false) List<String> hearingStatus) {
        return unNotifiedHearingService.getUnNotifiedHearings(
                hmctsServiceCode,
                hearingStartDateFrom,
                hearingStartDateTo,
                hearingStatus);
    }
}
