package uk.gov.hmcts.reform.hmc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.TestFixtures;
import uk.gov.hmcts.reform.hmc.data.ActualHearingDayEntity;
import uk.gov.hmcts.reform.hmc.data.ActualHearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingStatusAuditEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.model.ActualHearingDay;
import uk.gov.hmcts.reform.hmc.model.HearingActual;
import uk.gov.hmcts.reform.hmc.model.HearingActualsOutcome;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingStatusAuditRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static uk.gov.hmcts.reform.hmc.constants.Constants.POST_HEARING_ACTUALS_COMPLETION;
import static uk.gov.hmcts.reform.hmc.constants.Constants.PUT_HEARING_ACTUALS_COMPLETION;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.ADJOURNED;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.CANCELLED;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.COMPLETED;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.LISTED;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HA_OUTCOME_RESULT_NOT_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_HEARING_DAYS_INVALID;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_NON_UNIQUE_HEARING_DAYS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.NO_PREVIOUS_HEARING_ACTUALS_RECORDED;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PUT_HEARING_ACTUALS_INVALID_STATUS;

class HearingActualsServiceIT extends BaseTest {

    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";
    private static final String HEARING_ACTUALS_DATA_SCRIPT = "classpath:sql/insert-actualhearings.sql";

    private String clientS2SToken = "client-token";

    private final HearingActualsService hearingActualsService;
    private final HearingStatusAuditRepository hearingStatusAuditRepository;
    private final HearingRepository hearingRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    HearingActualsServiceIT(HearingActualsService hearingActualsService,
                            HearingStatusAuditRepository hearingStatusAuditRepository,
                            HearingRepository hearingRepository) {
        this.hearingActualsService = hearingActualsService;
        this.hearingStatusAuditRepository = hearingStatusAuditRepository;
        this.hearingRepository = hearingRepository;
    }

    @Nested
    @DisplayName("updateHearingActuals")
    class UpdateHearingActuals {

        //AC01a
        @ParameterizedTest
        @MethodSource("inValidHearingStatus")
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, HEARING_ACTUALS_DATA_SCRIPT})
        void whenHearingStatusIsInValid(Long hearingId) {
            HearingActual request = buildValidActualRequest();
            Optional<HearingEntity> hearingEntity = hearingRepository.findById(hearingId);
            BadRequestException exception =
                assertThrows(
                    BadRequestException.class,
                    () -> hearingActualsService.updateHearingActuals(
                        hearingId, clientS2SToken,
                        request
                    )
                );

            assertEquals(
                String.format(PUT_HEARING_ACTUALS_INVALID_STATUS, hearingEntity.get().getStatus()),
                exception.getMessage()
            );
        }

        private static Stream<Arguments> inValidHearingStatus() {
            return Stream.of(
                arguments(2000000000L),
                arguments(2000000001L),
                arguments(2000000002L),
                arguments(2000000003L)
            );
        }

        //AC01b- hearingOutcome.hearingResult is ADJOURNED
        @ParameterizedTest
        @MethodSource("inValidHearingStatusAndValidHearingResult")
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, HEARING_ACTUALS_DATA_SCRIPT})
        void whenInValidHearingStatusAndValidHearingResult(Long hearingId) {
            HearingActual request = buildValidActualRequest();
            request.getHearingOutcome().setHearingResult(ADJOURNED.name());
            Optional<HearingEntity> hearingEntity = hearingRepository.findById(hearingId);
            BadRequestException exception =
                assertThrows(
                    BadRequestException.class,
                    () -> hearingActualsService.updateHearingActuals(
                        hearingId, clientS2SToken,
                        request
                    )
                );

            assertEquals(
                String.format(PUT_HEARING_ACTUALS_INVALID_STATUS, hearingEntity.get().getStatus()),
                exception.getMessage()
            );
        }

        private static Stream<Arguments> inValidHearingStatusAndValidHearingResult() {
            return Stream.of(
                arguments(2000000000L),
                arguments(2000000001L),
                arguments(2000000002L),
                arguments(2000000003L)
            );
        }

        //AC02
        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, HEARING_ACTUALS_DATA_SCRIPT})
        void whenValidHearingStatusMissingHearingActuals() throws JsonProcessingException {
            Long hearingId = 2000000012L;
            String json = TestFixtures.fromFileAsString("hearing-actuals-payload/HMAN80-ValidPayload1.json");
            HearingActual request = objectMapper.readValue(json, HearingActual.class);
            BadRequestException exception =
                assertThrows(
                    BadRequestException.class,
                    () -> hearingActualsService.updateHearingActuals(hearingId, clientS2SToken, request)
                );
            assertEquals(NO_PREVIOUS_HEARING_ACTUALS_RECORDED, exception.getMessage());
        }

        //AC03
        @ParameterizedTest
        @MethodSource("hearingDayInFuture")
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, HEARING_ACTUALS_DATA_SCRIPT})
        void whenActualHearingDayInFuture(Long hearingId) {
            HearingActual request = buildValidActualRequest();
            request.getActualHearingDays().get(0).setHearingDate(LocalDate.now().plusDays(2));
            request.getActualHearingDays().get(0).setHearingStartTime(LocalDate.now().atTime(10, 0));
            request.getActualHearingDays().get(0).setHearingEndTime(LocalDate.now().atTime(11, 0));
            BadRequestException exception =
                assertThrows(
                    BadRequestException.class,
                    () -> hearingActualsService.updateHearingActuals(hearingId, clientS2SToken, request)
                );
            assertEquals(HEARING_ACTUALS_HEARING_DAYS_INVALID, exception.getMessage());
        }

        private static Stream<Arguments> hearingDayInFuture() {
            return Stream.of(
                arguments(2000000004L),
                arguments(2000000005L),
                arguments(2000000006L),
                arguments(2000000007L)
            );
        }

        //AC04
        @ParameterizedTest
        @MethodSource("duplicateHearingDates")
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, HEARING_ACTUALS_DATA_SCRIPT})
        void whenActualHearingDayDateIsNonUnique(Long hearingId) throws JsonProcessingException {
            String json = TestFixtures.fromFileAsString("hearing-actuals-payload/HMAN1100-DuplicateHearingDays.json");
            HearingActual request = objectMapper.readValue(json, HearingActual.class);
            BadRequestException exception =
                assertThrows(
                    BadRequestException.class,
                    () -> hearingActualsService.updateHearingActuals(hearingId, clientS2SToken, request)
                );
            assertEquals(HEARING_ACTUALS_NON_UNIQUE_HEARING_DAYS, exception.getMessage());
        }

        private static Stream<Arguments> duplicateHearingDates() {
            return Stream.of(
                arguments(2000000008L),
                arguments(2000000009L)
            );
        }

        //AC05
        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, HEARING_ACTUALS_DATA_SCRIPT})
        void whenHearingStatusIsListed() throws JsonProcessingException {
            Long hearingId = 2000000010L;
            String json = TestFixtures.fromFileAsString("hearing-actuals-payload/HMAN80-ValidPayload1.json");
            HearingActual request = objectMapper.readValue(json, HearingActual.class);
            hearingActualsService.updateHearingActuals(hearingId, clientS2SToken, request);
            Optional<HearingEntity> hearingEntityOptional = hearingRepository.findById(hearingId);
            HearingEntity hearingEntity = hearingEntityOptional.get();
            assertEquals(LISTED.name(), hearingEntity.getStatus());
            validateActualHearings(hearingEntity, request);
            validatePutHearingActualsAuditInfo(hearingId, LISTED.name());
            //verify publish message
        }

        //AC06
        @ParameterizedTest
        @MethodSource("finalHearingStatuses")
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, HEARING_ACTUALS_DATA_SCRIPT})
        void whenHearingStatusIsFinal(Long hearingId, String putHearingEventStatus, String postHearingEventStatus)
            throws JsonProcessingException {
            String json = TestFixtures.fromFileAsString("hearing-actuals-payload/HMAN80-ValidPayload1.json");
            HearingActual request = objectMapper.readValue(json, HearingActual.class);
            request.getHearingOutcome().setHearingResult(postHearingEventStatus);
            hearingActualsService.updateHearingActuals(hearingId, clientS2SToken, request);
            Optional<HearingEntity> hearingEntityOptional = hearingRepository.findById(hearingId);
            HearingEntity hearingEntity = hearingEntityOptional.get();
            assertEquals(postHearingEventStatus, hearingEntity.getStatus());
            validateActualHearings(hearingEntity, request);
            validatePostHearingActualsAuditInfo(hearingId, putHearingEventStatus, postHearingEventStatus);
            //verify publish message
        }

        private static Stream<Arguments> finalHearingStatuses() {
            return Stream.of(
                arguments(2000000011L, COMPLETED.name(), COMPLETED.name()),
                arguments(2000000013L, CANCELLED.name(), CANCELLED.name()),
                arguments(2000000014L, ADJOURNED.name(), ADJOURNED.name()),
                arguments(2000000011L, COMPLETED.name(), CANCELLED.name()),
                arguments(2000000013L, CANCELLED.name(), COMPLETED.name()),
                arguments(2000000014L, ADJOURNED.name(), CANCELLED.name()),
                arguments(2000000011L, COMPLETED.name(), ADJOURNED.name())
            );
        }

        //AC07
        @ParameterizedTest
        @MethodSource("finalHearingStatusesAndInvalidHearingResult")
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, HEARING_ACTUALS_DATA_SCRIPT})
        void whenHearingStatusIsFinalAndHearingResultIsInvalid(Long hearingId, String putHearingEventStatus)
            throws JsonProcessingException {
            String json = TestFixtures.fromFileAsString("hearing-actuals-payload/HMAN80-ValidPayload1.json");
            HearingActual request = objectMapper.readValue(json, HearingActual.class);
            request.getHearingOutcome().setHearingResult(putHearingEventStatus);
            hearingActualsService.updateHearingActuals(hearingId, clientS2SToken, request);
            Optional<HearingEntity> hearingEntityOptional = hearingRepository.findById(hearingId);
            HearingEntity hearingEntity = hearingEntityOptional.get();
            assertEquals(putHearingEventStatus, hearingEntity.getStatus());
            validateActualHearings(hearingEntity, request);
        }

        private static Stream<Arguments> finalHearingStatusesAndInvalidHearingResult() {
            return Stream.of(
                arguments(2000000011L, COMPLETED.name(), COMPLETED.name()),
                arguments(2000000013L, CANCELLED.name(), CANCELLED.name()),
                arguments(2000000014L, ADJOURNED.name(), ADJOURNED.name()),
                arguments(2000000011L, COMPLETED.name(), CANCELLED.name()),
                arguments(2000000013L, CANCELLED.name(), COMPLETED.name()),
                arguments(2000000014L, ADJOURNED.name(), CANCELLED.name()),
                arguments(2000000011L, COMPLETED.name(), ADJOURNED.name())
            );
        }

        @ParameterizedTest
        @MethodSource("inValidActualHearingDay")
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, HEARING_ACTUALS_DATA_SCRIPT})
        void whenHearingDayIsLessThanResponseStartTime(Long hearingId) throws JsonProcessingException {
            String json = TestFixtures.fromFileAsString("hearing-actuals-payload/HMAN80-ValidPayload1.json");
            HearingActual request = objectMapper.readValue(json, HearingActual.class);
            BadRequestException exception =
                assertThrows(
                    BadRequestException.class,
                    () -> hearingActualsService.updateHearingActuals(hearingId, clientS2SToken, request)
                );
            assertEquals(HEARING_ACTUALS_HEARING_DAYS_INVALID, exception.getMessage());
        }

        private static Stream<Arguments> inValidActualHearingDay() {
            return Stream.of(
                arguments(2000000004L),
                arguments(2000000005L)
            );
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, HEARING_ACTUALS_DATA_SCRIPT})
        void whenHearingResultIsNull() throws JsonProcessingException {
            String json = TestFixtures.fromFileAsString("hearing-actuals-payload/HMAN1100-InvalidHearingResult.json");
            HearingActual request = objectMapper.readValue(json, HearingActual.class);
            BadRequestException exception =
                assertThrows(
                    BadRequestException.class,
                    () -> hearingActualsService.updateHearingActuals(2000000010L, clientS2SToken, request)
                );
            assertEquals(HA_OUTCOME_RESULT_NOT_EMPTY, exception.getMessage());
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, HEARING_ACTUALS_DATA_SCRIPT})
        void whenHearingResultReasonIsInvalid() throws JsonProcessingException {
            String json = TestFixtures.fromFileAsString("hearing-actuals-payload/HMAN80-ValidPayload1.json");
            HearingActual request = objectMapper.readValue(json, HearingActual.class);
            request.getHearingOutcome().setHearingResult("Invalid Reason");
            BadRequestException exception =
                assertThrows(
                    BadRequestException.class,
                    () -> hearingActualsService.updateHearingActuals(2000000010L, clientS2SToken, request)
                );
            assertEquals(HA_OUTCOME_RESULT_NOT_EMPTY, exception.getMessage());
        }
    }

    private void validatePutHearingActualsAuditInfo(Long hearingId, String expectedStatus) {
        List<HearingStatusAuditEntity> auditEntityList = hearingStatusAuditRepository
            .findByHearingId(hearingId.toString());
        Optional<HearingStatusAuditEntity> auditEntity = auditEntityList.stream()
            .filter(audit -> PUT_HEARING_ACTUALS_COMPLETION.equals(audit.getHearingEvent()))
            .findFirst();
        assertNotNull(auditEntityList);
        assertEquals(expectedStatus, auditEntity.get().getStatus());
        assertNotNull(auditEntity.get().getResponseDateTime());
        assertEquals(PUT_HEARING_ACTUALS_COMPLETION, auditEntity.get().getHearingEvent());
        assertNull(auditEntity.get().getOtherInfo());
    }

    private void validatePostHearingActualsAuditInfo(Long hearingId, String putHearingEventStatus,
                                                     String postHearingEventStatus) {

        validatePutHearingActualsAuditInfo(hearingId, putHearingEventStatus);

        List<HearingStatusAuditEntity> auditEntityList = hearingStatusAuditRepository
            .findByHearingId(hearingId.toString());
        Optional<HearingStatusAuditEntity> auditEntity = auditEntityList.stream()
            .filter(audit -> POST_HEARING_ACTUALS_COMPLETION.equals(audit.getHearingEvent()))
            .findFirst();
        assertNotNull(auditEntityList);
        assertEquals(postHearingEventStatus, auditEntity.get().getStatus());
        assertNotNull(auditEntity.get().getResponseDateTime());

        assertNotNull(auditEntity.get().getOtherInfo());
        assertEquals("user@hmcts.net", auditEntity.get().getOtherInfo().get("userId").asText());
        assertEquals(POST_HEARING_ACTUALS_COMPLETION, auditEntity.get().getHearingEvent());
        assertEquals(HttpStatus.SC_OK, Integer.parseInt(auditEntity.get().getHttpStatus()));
    }

    private void validateActualHearings(HearingEntity hearingEntity, HearingActual request) {
        ActualHearingEntity actualHearingEntity = hearingEntity.getHearingResponses().get(0).getActualHearingEntity();

        assertEquals(request.getActualHearingDays().size(), actualHearingEntity.getActualHearingDay().size());

        assertEquals(request.getHearingOutcome().getHearingType(), actualHearingEntity.getActualHearingType());
        assertEquals(
            request.getHearingOutcome().getHearingFinalFlag(),
            actualHearingEntity.getActualHearingIsFinalFlag()
        );
        assertEquals(
            request.getHearingOutcome().getHearingResult(),
            actualHearingEntity.getHearingResultType().name()
        );
        assertEquals(
            request.getHearingOutcome().getHearingResultReasonType(),
            actualHearingEntity.getHearingResultReasonType()
        );
        assertEquals(
            request.getHearingOutcome().getHearingResultDate(),
            actualHearingEntity.getHearingResultDate()
        );

        ActualHearingDayEntity actualHearingDayEntity = actualHearingEntity.getActualHearingDay().get(0);
        ActualHearingDay firstActualHearingDay = request.getActualHearingDays().get(0);
        assertEquals(firstActualHearingDay.getHearingDate(), actualHearingDayEntity.getHearingDate());
        assertEquals(firstActualHearingDay.getHearingStartTime(), actualHearingDayEntity.getStartDateTime());
        assertEquals(firstActualHearingDay.getHearingEndTime(), actualHearingDayEntity.getEndDateTime());
        assertEquals(
            request.getActualHearingDays().get(1).getHearingDate(),
            actualHearingEntity.getActualHearingDay().get(1).getHearingDate()
        );
        assertEquals(
            request.getActualHearingDays().get(2).getHearingEndTime(),
            actualHearingEntity.getActualHearingDay().get(2).getEndDateTime()
        );

    }

    private HearingActual buildValidActualRequest() {
        HearingActualsOutcome outcome = new HearingActualsOutcome();
        outcome.setHearingType("Civil hearing");
        outcome.setHearingFinalFlag(Boolean.FALSE);
        outcome.setHearingResultDate(LocalDate.now());

        HearingActual request = new HearingActual();
        request.setHearingOutcome(outcome);

        ActualHearingDay actualHearingDay = new ActualHearingDay();
        actualHearingDay.setHearingDate(LocalDate.now());
        actualHearingDay.setHearingStartTime(LocalDate.now().atTime(10, 0));
        actualHearingDay.setHearingEndTime(LocalDate.now().atTime(11, 0));
        request.setActualHearingDays(List.of(actualHearingDay));
        return request;
    }

}

