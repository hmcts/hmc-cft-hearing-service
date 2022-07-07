package uk.gov.hmcts.reform.hmc.service;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import uk.gov.hmcts.reform.hmc.model.RoomAttribute;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("java:S5778")
class RoomAttributesServiceImplTest {

    @Nested
    class WithValidFile {

        private RoomAttributesServiceImpl roomAttributesService;

        @BeforeEach
        void setUp() throws IOException {
            roomAttributesService = new RoomAttributesServiceImpl(getResource("valid-file.json"));
        }

        @Test
        void shouldFindByReasonableAdjustmentCode() {
            Optional<RoomAttribute> roomAttribute =
                roomAttributesService.findByReasonableAdjustmentCode("ReasonableAdjustment2");

            assertTrue(roomAttribute.isPresent());
            assertThat(roomAttribute.get().getRoomAttributeCode(), is("RoomCode2"));
            assertThat(roomAttribute.get().getRoomAttributeName(), is("Name2"));
            assertThat(roomAttribute.get().getReasonableAdjustmentCode(), is("ReasonableAdjustment2"));
            assertThat(roomAttribute.get().isFacility(), is(true));
        }

        @Test
        void shouldReturnEmptyOptionalWhenNoReasonableAdjustmentCodeFound() {
            Optional<RoomAttribute> roomAttribute =
                roomAttributesService.findByReasonableAdjustmentCode("NonExistingCode");

            assertTrue(roomAttribute.isEmpty());
        }

        @Test
        void shouldReturnEmptyOptionalWhenNullReasonableAdjustmentCodeProvided() {
            Optional<RoomAttribute> roomAttribute =
                roomAttributesService.findByReasonableAdjustmentCode(null);

            assertTrue(roomAttribute.isEmpty());
        }

        @Test
        void shouldFindByRoomAttributeCode() {
            Optional<RoomAttribute> roomAttribute =
                roomAttributesService.findByRoomAttributeCode("RoomCode3");

            assertTrue(roomAttribute.isPresent());
            assertThat(roomAttribute.get().getRoomAttributeCode(), is("RoomCode3"));
            assertThat(roomAttribute.get().getRoomAttributeName(), is("Name3"));
            assertThat(roomAttribute.get().getReasonableAdjustmentCode(), is("ReasonableAdjustment3"));
            assertThat(roomAttribute.get().isFacility(), is(false));
        }

        @Test
        void shouldFindByRoomAttributeCodeWhereOptionalFieldsNotSet() {
            Optional<RoomAttribute> roomAttribute =
                roomAttributesService.findByRoomAttributeCode("RoomCode4");

            assertTrue(roomAttribute.isPresent());
            assertThat(roomAttribute.get().getRoomAttributeCode(), is("RoomCode4"));
            assertNull(roomAttribute.get().getRoomAttributeName());
            assertNull(roomAttribute.get().getReasonableAdjustmentCode());
            assertThat(roomAttribute.get().isFacility(), is(true));
        }

        @Test
        void shouldReturnEmptyOptionalWhenNoRoomAttributeCodeFound() {
            Optional<RoomAttribute> roomAttribute =
                roomAttributesService.findByRoomAttributeCode("NonExistingCode");

            assertTrue(roomAttribute.isEmpty());
        }

        @Test
        void shouldReturnEmptyOptionalWhenNullRoomAttributeCodeProvided() {
            Optional<RoomAttribute> roomAttribute =
                roomAttributesService.findByRoomAttributeCode(null);

            assertTrue(roomAttribute.isEmpty());
        }
    }

    @Nested
    class WithInvalidFile {

        @Test
        void shouldFailWhenDuplicateReasonableAdjustmentCodesProvided() {
            IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                new RoomAttributesServiceImpl(getResource("duplicate-reasonable-adjustments.json")));

            assertThat(exception.getMessage(), startsWith("Duplicate key DuplicateReasonableAdjustment"));
        }

        @Test
        void shouldFailWhenDuplicateRoomAttributeCodesProvided() {
            IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                new RoomAttributesServiceImpl(getResource("duplicate-room-attributes.json")));

            assertThat(exception.getMessage(), startsWith("Duplicate key DuplicateRoomAttributeCode"));
        }

        @Test
        void shouldFailWhenMissingRequiredJsonField() {
            MismatchedInputException exception = assertThrows(MismatchedInputException.class, () ->
                new RoomAttributesServiceImpl(getResource("missing-json-field.json")));

            assertThat(exception.getMessage(), startsWith("Missing required creator property 'facility'"));
        }

        @Test
        void shouldFailWhenUnexpectedJsonFieldProvided() {
            MismatchedInputException exception = assertThrows(MismatchedInputException.class, () ->
                new RoomAttributesServiceImpl(getResource("unexpected-json-field.json")));

            assertThat(exception.getMessage(), startsWith("Unrecognized field \"unexpectedField\""));
        }

        @Test
        void shouldFailWhenProvidedFileDoesNotExist() {
            FileNotFoundException exception = assertThrows(FileNotFoundException.class, () ->
                new RoomAttributesServiceImpl(getResource("non-existing-file.json")));

            assertThat(exception.getMessage(), is("class path resource [room-attributes/non-existing-file.json]"
                                                      + " cannot be resolved to URL because it does not exist"));
        }
    }

    private Resource getResource(String fileName) {
        return new ClassPathResource("room-attributes/" + fileName);
    }
}
