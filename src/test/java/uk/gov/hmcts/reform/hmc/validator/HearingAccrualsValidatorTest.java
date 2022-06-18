package uk.gov.hmcts.reform.hmc.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.hmc.data.ActualHearingEntity;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.model.HearingResultType;
import uk.gov.hmcts.reform.hmc.repository.ActualHearingRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class HearingAccrualsValidatorTest {

    @InjectMocks
    private HearingIdValidator hearingIdValidator;

    @InjectMocks
    private HearingAccrualsValidator hearingAccrualsValidator;

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private ActualHearingRepository actualHearingRepository;

    private static final Long VALID_HEARING_ID = 2000000000L;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        hearingAccrualsValidator = new HearingAccrualsValidator(hearingIdValidator);
    }

    @Test
    void testExpectedException_BadOutcomeInfo() {
        HearingEntity expectedHearing = generateHearing(VALID_HEARING_ID,
                PutHearingStatus.HEARING_REQUESTED.name(),
                null, 1L);
        expectedHearing.setCaseHearingRequests(generateCaseHearingRequests(expectedHearing));
        when(hearingRepository.findById(VALID_HEARING_ID)).thenReturn(Optional.of(expectedHearing));
        when(hearingRepository.existsById(VALID_HEARING_ID)).thenReturn(true);
        Exception exception = assertThrows(BadRequestException.class, () -> hearingAccrualsValidator
                .validateHearingOutcomeInformation(VALID_HEARING_ID));
        assertTrue(exception.getMessage().contains(ValidationError.HEARING_ACTUALS_MISSING_HEARING_OUTCOME));
    }

    @Test
    void testNoException_BadOutcomeInfoIsGood() {
        HearingEntity expectedHearing = generateHearing(VALID_HEARING_ID,
                PutHearingStatus.HEARING_REQUESTED.name(),
                null, 1L);
        expectedHearing.setCaseHearingRequests(generateCaseHearingRequests(expectedHearing));
        Optional<ActualHearingEntity> actualHearing = Optional.of(generateActualHearing(VALID_HEARING_ID));
        generateHearingResponseEntity(1, expectedHearing,
                actualHearing.get());
        when(hearingRepository.findById(VALID_HEARING_ID)).thenReturn(Optional.of(expectedHearing));
        when(hearingRepository.existsById(VALID_HEARING_ID)).thenReturn(true);
        when(actualHearingRepository.findByHearingResponse(any())).thenReturn(actualHearing);
        hearingAccrualsValidator
                .validateHearingOutcomeInformation(VALID_HEARING_ID);
    }

    @Test
    void shouldThrowExceptionWhenHasHearingResultOfAdjournedWithoutHearingResultReasonType() {

        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingAccrualsValidator.validateHearingResult("ADJOURNED", null);
        });
        assertTrue(exception.getMessage().contains("ADJOURNED result requires a hearingResultReasonType"));
    }

    protected void generateHearingResponseEntity(Integer requestVersion, HearingEntity hearingEntity,
                                               ActualHearingEntity actualHearingEntity) {
        HearingResponseEntity responseEntity = new HearingResponseEntity();
        responseEntity.setCancellationReasonType("Test Reason Type");
        responseEntity.setRequestVersion(requestVersion);
        responseEntity.setHearing(hearingEntity);
        responseEntity.setActualHearingEntity(actualHearingEntity);
        hearingEntity.setHearingResponses(List.of(responseEntity));
        actualHearingEntity.setHearingResponse(responseEntity);
    }

    protected ActualHearingEntity generateActualHearing(Long id) {
        ActualHearingEntity actualHearing = new ActualHearingEntity();
        actualHearing.setActualHearingId(id);
        actualHearing.setActualHearingIsFinalFlag(true);
        actualHearing.setActualHearingType("Test 111");
        actualHearing.setHearingResultType(HearingResultType.ADJOURNED);
        actualHearing.setHearingResultReasonType("REASON TYPE 78");
        actualHearing.setHearingResultDate(LocalDate.now().plusDays(7));
        return actualHearing;
    }

    protected List<CaseHearingRequestEntity> generateCaseHearingRequests(HearingEntity hearing) {
        List<CaseHearingRequestEntity> caseHearingRequests = new ArrayList<>();
        CaseHearingRequestEntity request1 = generateCaseHearingRequest(hearing,
                1, LocalDateTime.now().plusDays(1));
        CaseHearingRequestEntity request2 = generateCaseHearingRequest(hearing,
                1, LocalDateTime.now().plusDays(2));
        caseHearingRequests.add(request1);
        caseHearingRequests.add(request2);
        return caseHearingRequests;
    }

    protected HearingEntity generateHearing(Long id, String status, LinkedGroupDetails groupDetails, Long linkedOrder) {
        HearingEntity hearing = new HearingEntity();
        hearing.setId(id);
        hearing.setStatus(status);
        hearing.setLinkedGroupDetails(groupDetails);
        if (null == groupDetails) {
            hearing.setIsLinkedFlag(true);
        }
        hearing.setLinkedOrder(linkedOrder);
        return hearing;
    }

    protected CaseHearingRequestEntity generateCaseHearingRequest(HearingEntity hearing, Integer version,
                                                                LocalDateTime receivedDateTime) {
        CaseHearingRequestEntity request = new CaseHearingRequestEntity();
        request.setHearing(hearing);
        request.setVersionNumber(version);
        request.setHearingRequestReceivedDateTime(receivedDateTime);
        return request;
    }

}
