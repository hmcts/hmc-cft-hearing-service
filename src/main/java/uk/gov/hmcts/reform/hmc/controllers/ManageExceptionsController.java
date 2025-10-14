package uk.gov.hmcts.reform.hmc.controllers;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.model.ManageExceptionRequest;
import uk.gov.hmcts.reform.hmc.model.ManageExceptionResponse;
import uk.gov.hmcts.reform.hmc.service.ManageExceptionsService;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.hmc.data.SecurityUtils.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_MANAGE_EXCEPTION_ROLE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_MANAGE_HEARING_SERVICE_EXCEPTION;

@RestController
@Validated
public class ManageExceptionsController {

    private final ManageExceptionsService manageExceptionsService;

    public ManageExceptionsController(ManageExceptionsService manageExceptionsService) {
        this.manageExceptionsService = manageExceptionsService;
    }

    @PostMapping(path = "/manageExceptions", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "200", description = "Hearing successfully transitioned")
    @ApiResponse(responseCode = "400", description = "One or more of the following reasons:"
        + "\n1) " + ValidationError.INVALID_HEARING_ID_LIMIT
        + "\n2) " + ValidationError.DUPLICATE_HEARING_IDS
        + "\n3) " + ValidationError.CASE_REFERENCE_INVALID
        + "\n3) " + ValidationError.INVALID_HEARING_STATE)
    @ApiResponse(responseCode = "401", description = INVALID_MANAGE_EXCEPTION_ROLE)
    @ApiResponse(responseCode = "403", description = INVALID_MANAGE_HEARING_SERVICE_EXCEPTION)

    public ManageExceptionResponse manageExceptions(@RequestHeader(SERVICE_AUTHORIZATION) String clientS2SToken,
                                                    @RequestBody @Valid ManageExceptionRequest supportRequest) {
        return manageExceptionsService.manageExceptions(supportRequest, clientS2SToken);
    }

}
