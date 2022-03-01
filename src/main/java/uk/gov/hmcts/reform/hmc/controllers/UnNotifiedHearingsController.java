package uk.gov.hmcts.reform.hmc.controllers;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import uk.gov.hmcts.reform.hmc.model.UnNotifiedHearingsResponse;
import uk.gov.hmcts.reform.hmc.service.UnNotifiedHearingService;

import java.time.LocalDate;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_START_DATE_FROM;
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
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 401, message = "Unauthorised"),
        @ApiResponse(code = 403, message = "Forbidden")
    })
    public UnNotifiedHearingsResponse getUnNotifiedHearings(@PathVariable("hmctsServiceCode")
                                                            @Valid
                                                            @NotEmpty(message = HMCTS_SERVICE_CODE_EMPTY)
                                                                String hmctsServiceCode,
                                                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                            @RequestParam(name = "hearing_start_date_from")
                                                                LocalDate hearingStartDateFrom,
                                                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                            @RequestParam(name = "hearing_start_date_to",
                                                                required = false)
                                                                LocalDate hearingStartDateTo) {
        return unNotifiedHearingService.getUnNotifiedHearings(hmctsServiceCode, hearingStartDateFrom,
                                                              hearingStartDateTo
        );
    }
}

