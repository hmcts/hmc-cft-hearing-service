package uk.gov.hmcts.reform.hmc.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.model.RoomAttribute;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestPropertySource(properties = { "room-attributes.file = classpath:room-attributes/valid-file.json" })
class RoomAttributesServiceIT extends BaseTest {

    @Autowired
    private RoomAttributesService roomAttributesService;

    @Test
    void shouldPopulateRoomAttributeCodeMapFromFileOnStartup() {
        Optional<RoomAttribute> roomAttribute =
            roomAttributesService.findByRoomAttributeCode("RoomCode3");

        assertTrue(roomAttribute.isPresent());
        assertThat(roomAttribute.get().getRoomAttributeCode(), is("RoomCode3"));
        assertThat(roomAttribute.get().getRoomAttributeName(), is("Name3"));
        assertThat(roomAttribute.get().getReasonableAdjustmentCode(), is("ReasonableAdjustment3"));
        assertThat(roomAttribute.get().isFacility(), is(false));
    }

    @Test
    void shouldPopulateReasonableAdjustmentCodeMapFromFileOnStartup() {
        Optional<RoomAttribute> roomAttribute =
            roomAttributesService.findByReasonableAdjustmentCode("ReasonableAdjustment2");

        assertTrue(roomAttribute.isPresent());
        assertThat(roomAttribute.get().getRoomAttributeCode(), is("RoomCode2"));
        assertThat(roomAttribute.get().getRoomAttributeName(), is("Name2"));
        assertThat(roomAttribute.get().getReasonableAdjustmentCode(), is("ReasonableAdjustment2"));
        assertThat(roomAttribute.get().isFacility(), is(true));
    }
}
