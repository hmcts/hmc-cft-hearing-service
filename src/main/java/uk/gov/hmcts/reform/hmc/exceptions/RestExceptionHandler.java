package uk.gov.hmcts.reform.hmc.exceptions;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolationException;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    static final String BAD_REQUEST_EXCEPTION = "BadRequestException";

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String[] errors = ex.getBindingResult().getFieldErrors().stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .toArray(String[]::new);
        log.debug("MethodArgumentNotValidException:{}", ex.getLocalizedMessage());
        return toResponseEntity(status, errors);
    }

    @ExceptionHandler(BadRequestException.class)
    protected ResponseEntity<Object> handleBadRequestException(BadRequestException ex) {
        log.debug(BAD_REQUEST_EXCEPTION + ": {}", ex.getLocalizedMessage());
        return toResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(FhBadRequestException.class)
    protected ResponseEntity<Object> handleBadRequestException(FhBadRequestException ex) {
        log.debug(BAD_REQUEST_EXCEPTION + ": {}", ex.getLocalizedMessage());
        return toResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<Object> handleBadRequestException(IllegalArgumentException ex) {
        log.debug("IllegalArgumentException: {}", ex.getLocalizedMessage());
        return toResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InvalidRoleAssignmentException.class)
    protected ResponseEntity<Object> handleBadRequestException(InvalidRoleAssignmentException ex) {
        log.debug("InvalidRoleAssignmentException: {}", ex.getLocalizedMessage());
        return toResponseEntity(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.error("Resource could not be found: {}", ex.getMessage(), ex);
        return toResponseEntity(HttpStatus.FORBIDDEN, ex.getLocalizedMessage());
    }

    @ExceptionHandler(CaseCouldNotBeFoundException.class)
    public ResponseEntity<Object> handleCaseCouldNotBeFoundException(CaseCouldNotBeFoundException ex) {
        log.error("Case could not be found: {}", ex.getMessage(), ex);
        return toResponseEntity(HttpStatus.FORBIDDEN, ex.getLocalizedMessage());
    }

    @ExceptionHandler(ServiceException.class)
    protected ResponseEntity<Object> handleServiceException(ServiceException ex) {
        log.debug(BAD_REQUEST_EXCEPTION + ": {}", ex.getLocalizedMessage());
        return toResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Object> handleFeignStatusException(FeignException ex) {
        String errorMessage = ex.responseBody()
            .map(res -> new String(res.array(), StandardCharsets.UTF_8))
            .orElse(ex.getMessage());
        log.error("Downstream service errors: {}", errorMessage, ex);
        return toResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
    }

    @ExceptionHandler(HearingNotFoundException.class)
    protected ResponseEntity<Object> handleHearingNotFoundException(HearingNotFoundException ex) {
        log.debug("HearingNotFoundException:{}", ex.getLocalizedMessage());
        return toResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(PartiesNotifiedNotFoundException.class)
    protected ResponseEntity<Object> handlePartiesNotifyHearingNotFoundException(PartiesNotifiedNotFoundException ex) {
        log.debug("PartiesNotifiedNotFoundException:{}", ex.getLocalizedMessage());
        return toResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(PartiesNotifiedBadRequestException.class)
    protected ResponseEntity<Object> handlePartiesNotifiedBadRequestException(PartiesNotifiedBadRequestException ex) {
        log.debug("PartiesNotifiedBadRequestException:{}", ex.getLocalizedMessage());
        return toResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolationException(Exception ex) {
        log.debug(BAD_REQUEST_EXCEPTION + ":{}", ex.getLocalizedMessage());
        return toResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(LinkedGroupNotFoundException.class)
    protected ResponseEntity<Object> handleLinkedGroupNotFoundException(Exception ex) {
        log.debug("LinkedHearingGroupNotFoundException:{}", ex.getLocalizedMessage());
        return toResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(LinkedHearingNotValidForUnlinkingException.class)
    protected ResponseEntity<Object> handleLinkedHearingNotValidForUnlinkingException(Exception ex) {
        log.debug(BAD_REQUEST_EXCEPTION + ":{}", ex.getLocalizedMessage());
        return toResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InvalidManageHearingServiceException.class)
    protected ResponseEntity<Object> handleManageHearingServiceException(InvalidManageHearingServiceException ex) {
        log.debug("InvalidManageHearingServiceException: {}", ex.getLocalizedMessage());
        return toResponseEntity(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    private ResponseEntity<Object> toResponseEntity(HttpStatus status, String... errors) {
        List<String> errorList = Arrays.stream(errors).filter(Objects::nonNull).collect(Collectors.toList());
        var apiError = new ApiError(status, errorList);
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

}
