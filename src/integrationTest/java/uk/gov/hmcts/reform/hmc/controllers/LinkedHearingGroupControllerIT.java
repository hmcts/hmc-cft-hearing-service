package uk.gov.hmcts.reform.hmc.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.GroupDetails;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.LinkHearingDetails;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class LinkedHearingGroupControllerIT extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(LinkedHearingGroupControllerIT.class);

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationParams applicationParams;

    private static final String url = "/linkedHearingGroup";

    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";

    private static final String GET_HEARINGS_DATA_SCRIPT = "classpath:sql/insert-caseHearings_LinkedHearings.sql";

    @Nested
    @DisplayName("PostLinkedHearingGroup")
    class PostLinkedHearingGroup {
        @Test
        void shouldReturn400_WhenThereIsOnlyOneHearing() throws Exception {
            GroupDetails groupDetails = new GroupDetails();
            groupDetails.setGroupComments("comments");
            groupDetails.setGroupLinkType("Same Slot");
            groupDetails.setGroupName("name");
            groupDetails.setGroupReason("reason");

            LinkHearingDetails hearingInGroup = new LinkHearingDetails();
            hearingInGroup.setHearingId("2000000001");
            hearingInGroup.setHearingOrder(1);

            HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
            hearingLinkGroupRequest.setGroupDetails(groupDetails);
            hearingLinkGroupRequest.setHearingsInGroup(Arrays.asList(hearingInGroup));

            mockMvc.perform(post(url)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(hearingLinkGroupRequest)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasItem(ValidationError.HEARINGS_IN_GROUP_SIZE
                )))
                .andReturn();

        }

        @Test
        void shouldReturn400_WhenHearingIsNotUniqueInGroup() throws Exception {
            GroupDetails groupDetails = new GroupDetails();
            groupDetails.setGroupComments("comments");
            groupDetails.setGroupLinkType("Same Slot");
            groupDetails.setGroupName("name");
            groupDetails.setGroupReason("reason");

            LinkHearingDetails hearingInGroup = new LinkHearingDetails();
            hearingInGroup.setHearingId("2000000001");
            hearingInGroup.setHearingOrder(1);

            HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
            hearingLinkGroupRequest.setGroupDetails(groupDetails);
            hearingLinkGroupRequest.setHearingsInGroup(Arrays.asList(hearingInGroup, hearingInGroup));

            mockMvc.perform(post(url)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(hearingLinkGroupRequest)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasItem(ValidationError.HEARINGS_IN_GROUP_SIZE
                )))
                .andReturn();

        }

        @Test
        void shouldReturn404_WhenHearingDoesNotExist() throws Exception {
            GroupDetails groupDetails = new GroupDetails();
            groupDetails.setGroupComments("comments");
            groupDetails.setGroupLinkType("Same Slot");
            groupDetails.setGroupName("name");
            groupDetails.setGroupReason("reason");

            LinkHearingDetails hearingInGroup = new LinkHearingDetails();
            hearingInGroup.setHearingId("2000000001");
            hearingInGroup.setHearingOrder(1);

            LinkHearingDetails hearingInGroup1 = new LinkHearingDetails();
            hearingInGroup1.setHearingId("2000000002");
            hearingInGroup1.setHearingOrder(1);

            HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
            hearingLinkGroupRequest.setGroupDetails(groupDetails);
            hearingLinkGroupRequest.setHearingsInGroup(Arrays.asList(hearingInGroup, hearingInGroup1));

            mockMvc.perform(post(url)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(hearingLinkGroupRequest)))
                .andExpect(status().is(404))
                .andExpect(jsonPath("$.errors", hasItem("No hearing found for reference: 2000000001"
                )))
                .andReturn();

        }

        @Test
        void shouldReturn400_WhenHearingIsMalformed() throws Exception {
            GroupDetails groupDetails = new GroupDetails();
            groupDetails.setGroupComments("comments");
            groupDetails.setGroupLinkType("Same Slot");
            groupDetails.setGroupName("name");
            groupDetails.setGroupReason("reason");

            LinkHearingDetails hearingInGroup = new LinkHearingDetails();
            hearingInGroup.setHearingId("1000000001");
            hearingInGroup.setHearingOrder(1);

            LinkHearingDetails hearingInGroup1 = new LinkHearingDetails();
            hearingInGroup1.setHearingId("2000000002");
            hearingInGroup1.setHearingOrder(1);

            HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
            hearingLinkGroupRequest.setGroupDetails(groupDetails);
            hearingLinkGroupRequest.setHearingsInGroup(Arrays.asList(hearingInGroup, hearingInGroup1));

            mockMvc.perform(post(url)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(hearingLinkGroupRequest)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasItem(ValidationError.INVALID_HEARING_ID_DETAILS
                )))
                .andReturn();

        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
        void shouldReturn400_WhenHearingIsLinkedIsFalse() throws Exception {
            GroupDetails groupDetails = new GroupDetails();
            groupDetails.setGroupComments("comments");
            groupDetails.setGroupLinkType("Same Slot");
            groupDetails.setGroupName("name");
            groupDetails.setGroupReason("reason");

            LinkHearingDetails hearingInGroup = new LinkHearingDetails();
            hearingInGroup.setHearingId("2000000000");
            hearingInGroup.setHearingOrder(1);

            LinkHearingDetails hearingInGroup1 = new LinkHearingDetails();
            hearingInGroup1.setHearingId("2000000002");
            hearingInGroup1.setHearingOrder(1);

            HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
            hearingLinkGroupRequest.setGroupDetails(groupDetails);
            hearingLinkGroupRequest.setHearingsInGroup(Arrays.asList(hearingInGroup, hearingInGroup1));

            mockMvc.perform(post(url)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(hearingLinkGroupRequest)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasItem("002 hearing request isLinked is False"
                )))
                .andReturn();
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
        void shouldReturn400_WhenHearingRequestIsAlreadyInGroup() throws Exception {
            GroupDetails groupDetails = new GroupDetails();
            groupDetails.setGroupComments("comments");
            groupDetails.setGroupLinkType("Same Slot");
            groupDetails.setGroupName("name");
            groupDetails.setGroupReason("reason");

            LinkHearingDetails hearingInGroup = new LinkHearingDetails();
            hearingInGroup.setHearingId("2000000010");
            hearingInGroup.setHearingOrder(1);

            LinkHearingDetails hearingInGroup1 = new LinkHearingDetails();
            hearingInGroup1.setHearingId("2000000002");
            hearingInGroup1.setHearingOrder(1);

            HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
            hearingLinkGroupRequest.setGroupDetails(groupDetails);
            hearingLinkGroupRequest.setHearingsInGroup(Arrays.asList(hearingInGroup, hearingInGroup1));

            mockMvc.perform(post(url)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(hearingLinkGroupRequest)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasItem("003 hearing request already in a group"
                )))
                .andReturn();
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
        void shouldReturn400_WhenHearingIsInInvalidState() throws Exception {
            GroupDetails groupDetails = new GroupDetails();
            groupDetails.setGroupComments("comments");
            groupDetails.setGroupLinkType("Same Slot");
            groupDetails.setGroupName("name");
            groupDetails.setGroupReason("reason");

            LinkHearingDetails hearingInGroup = new LinkHearingDetails();
            hearingInGroup.setHearingId("2000000011");
            hearingInGroup.setHearingOrder(1);

            LinkHearingDetails hearingInGroup1 = new LinkHearingDetails();
            hearingInGroup1.setHearingId("2000000002");
            hearingInGroup1.setHearingOrder(1);

            HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
            hearingLinkGroupRequest.setGroupDetails(groupDetails);
            hearingLinkGroupRequest.setHearingsInGroup(Arrays.asList(hearingInGroup, hearingInGroup1));

            mockMvc.perform(post(url)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(hearingLinkGroupRequest)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasItem("004 Invalid state for hearing request 2000000011"
                )))
                .andReturn();
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
        void shouldReturn400_WhenHearingOrderIsNotUnique() throws Exception {
            GroupDetails groupDetails = new GroupDetails();
            groupDetails.setGroupComments("comments");
            groupDetails.setGroupLinkType("Ordered");
            groupDetails.setGroupName("name");
            groupDetails.setGroupReason("reason");

            LinkHearingDetails hearingInGroup = new LinkHearingDetails();
            hearingInGroup.setHearingId("2000000012");
            hearingInGroup.setHearingOrder(1);

            LinkHearingDetails hearingInGroup1 = new LinkHearingDetails();
            hearingInGroup1.setHearingId("2000000002");
            hearingInGroup1.setHearingOrder(1);

            HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
            hearingLinkGroupRequest.setGroupDetails(groupDetails);
            hearingLinkGroupRequest.setHearingsInGroup(Arrays.asList(hearingInGroup, hearingInGroup1));

            mockMvc.perform(post(url)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(hearingLinkGroupRequest)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasItem("005 Hearing Order is not unique"
                )))
                .andReturn();
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
        void shouldReturn201_WhenRequestIsValid() throws Exception {
            GroupDetails groupDetails = new GroupDetails();
            groupDetails.setGroupComments("comments");
            groupDetails.setGroupLinkType("Same Slot");
            groupDetails.setGroupName("name");
            groupDetails.setGroupReason("reason");

            LinkHearingDetails hearingInGroup = new LinkHearingDetails();
            hearingInGroup.setHearingId("2000000012");
            hearingInGroup.setHearingOrder(1);

            LinkHearingDetails hearingInGroup1 = new LinkHearingDetails();
            hearingInGroup1.setHearingId("2000000013");
            hearingInGroup1.setHearingOrder(2);

            HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
            hearingLinkGroupRequest.setGroupDetails(groupDetails);
            hearingLinkGroupRequest.setHearingsInGroup(Arrays.asList(hearingInGroup, hearingInGroup1));

            mockMvc.perform(post(url)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(hearingLinkGroupRequest)))
                .andExpect(status().is(201))
                .andReturn();
        }
    }
}
