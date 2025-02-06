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
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.domain.model.enums.LinkType;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.GroupDetails;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.LinkHearingDetails;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubDeleteLinkedHearingGroupsReturn4XX;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubDeleteLinkedHearingGroupsReturn5XX;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubPostCreateLinkHearingGroup;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubPutUpdateLinkHearingGroup;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubSuccessfullyDeleteLinkedHearingGroups;
import static uk.gov.hmcts.reform.hmc.data.SecurityUtils.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.LIST_ASSIST_FAILED_TO_RESPOND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.REJECTED_BY_LIST_ASSIST;

class LinkHearingGroupControllerIT extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(LinkHearingGroupControllerIT.class);

    public static final String ERROR_PATH_ERROR = "$.errors";

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private static final String URL = "/linkedHearingGroup";

    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";
    private static final String GET_HEARINGS_DATA_SCRIPT = "classpath:sql/insert-caseHearings_LinkedHearings.sql";
    private static final String INSERT_LINKED_HEARINGS_DATA_SCRIPT = "classpath:sql/insert-linked-hearings.sql";

    private static final String TOKEN = "example-token";
    private static final String HMI_REQUEST_URL = "/resources/linked-hearing-group";

    @Nested
    @DisplayName("PostLinkedHearingGroup")
    class PostLinkedHearingGroup {
        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
        void shouldReturn400_WhenThereIsOnlyOneHearing() throws Exception {
            LinkHearingDetails hearingInGroup = new LinkHearingDetails();
            hearingInGroup.setHearingId("2000000001");
            hearingInGroup.setHearingOrder(1);

            HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
            GroupDetails groupDetails = generateGroupDetails(LinkType.SAME_SLOT);
            hearingLinkGroupRequest.setGroupDetails(groupDetails);
            hearingLinkGroupRequest.setHearingsInGroup(Arrays.asList(hearingInGroup));

            mockMvc.perform(post(URL)
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingLinkGroupRequest)))
                    .andExpect(status().is(400))
                    .andExpect(jsonPath("$.errors", hasItem(ValidationError.HEARINGS_IN_GROUP_SIZE
                    )))
                    .andReturn();

        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
        void shouldReturn400_WhenHearingIsNotUniqueInGroup() throws Exception {
            LinkHearingDetails hearingInGroup = new LinkHearingDetails();
            hearingInGroup.setHearingId("2000000001");
            hearingInGroup.setHearingOrder(1);

            HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
            GroupDetails groupDetails = generateGroupDetails(LinkType.SAME_SLOT);
            hearingLinkGroupRequest.setGroupDetails(groupDetails);
            hearingLinkGroupRequest.setHearingsInGroup(Arrays.asList(hearingInGroup, hearingInGroup));

            mockMvc.perform(post(URL)
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingLinkGroupRequest)))
                    .andExpect(status().is(400))
                    .andExpect(jsonPath("$.errors", hasItem(ValidationError.HEARINGS_IN_GROUP_SIZE
                    )))
                    .andReturn();

        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
        void shouldReturn404_WhenHearingDoesNotExist() throws Exception {
            LinkHearingDetails hearingInGroup = new LinkHearingDetails();
            hearingInGroup.setHearingId("2000000001");
            hearingInGroup.setHearingOrder(1);

            LinkHearingDetails hearingInGroup1 = new LinkHearingDetails();
            hearingInGroup1.setHearingId("2000000002");
            hearingInGroup1.setHearingOrder(1);

            HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
            GroupDetails groupDetails = generateGroupDetails(LinkType.SAME_SLOT);
            hearingLinkGroupRequest.setGroupDetails(groupDetails);
            hearingLinkGroupRequest.setHearingsInGroup(Arrays.asList(hearingInGroup, hearingInGroup1));

            mockMvc.perform(post(URL)
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingLinkGroupRequest)))
                    .andExpect(status().is(404))
                    .andExpect(jsonPath("$.errors", hasItem("No hearing found for reference: 2000000001"
                    )))
                    .andReturn();

        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
        void shouldReturn400_WhenHearingIsMalformed() throws Exception {
            LinkHearingDetails hearingInGroup = new LinkHearingDetails();
            hearingInGroup.setHearingId("1000000001");
            hearingInGroup.setHearingOrder(1);

            LinkHearingDetails hearingInGroup1 = new LinkHearingDetails();
            hearingInGroup1.setHearingId("2000000002");
            hearingInGroup1.setHearingOrder(1);

            HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
            GroupDetails groupDetails = generateGroupDetails(LinkType.SAME_SLOT);
            hearingLinkGroupRequest.setGroupDetails(groupDetails);
            hearingLinkGroupRequest.setHearingsInGroup(Arrays.asList(hearingInGroup, hearingInGroup1));

            mockMvc.perform(post(URL)
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
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
            LinkHearingDetails hearingInGroup = new LinkHearingDetails();
            hearingInGroup.setHearingId("2000000000");
            hearingInGroup.setHearingOrder(1);

            LinkHearingDetails hearingInGroup1 = new LinkHearingDetails();
            hearingInGroup1.setHearingId("2000000002");
            hearingInGroup1.setHearingOrder(1);

            HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
            GroupDetails groupDetails = generateGroupDetails(LinkType.SAME_SLOT);
            hearingLinkGroupRequest.setGroupDetails(groupDetails);
            hearingLinkGroupRequest.setHearingsInGroup(Arrays.asList(hearingInGroup, hearingInGroup1));

            mockMvc.perform(post(URL)
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
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
            LinkHearingDetails hearingInGroup1 = new LinkHearingDetails();
            hearingInGroup1.setHearingId("2000000005");
            hearingInGroup1.setHearingOrder(1);

            LinkHearingDetails hearingInGroup = new LinkHearingDetails();
            hearingInGroup.setHearingId("2000000008");
            hearingInGroup.setHearingOrder(1);

            HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
            GroupDetails groupDetails = generateGroupDetails(LinkType.SAME_SLOT);
            hearingLinkGroupRequest.setGroupDetails(groupDetails);
            hearingLinkGroupRequest.setHearingsInGroup(Arrays.asList(hearingInGroup, hearingInGroup1));

            mockMvc.perform(post(URL)
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
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
            LinkHearingDetails hearingInGroup = new LinkHearingDetails();
            hearingInGroup.setHearingId("2000000011");
            hearingInGroup.setHearingOrder(1);

            LinkHearingDetails hearingInGroup1 = new LinkHearingDetails();
            hearingInGroup1.setHearingId("2000000000");
            hearingInGroup1.setHearingOrder(1);

            HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
            GroupDetails groupDetails = generateGroupDetails(LinkType.SAME_SLOT);
            hearingLinkGroupRequest.setGroupDetails(groupDetails);
            hearingLinkGroupRequest.setHearingsInGroup(Arrays.asList(hearingInGroup, hearingInGroup1));

            mockMvc.perform(post(URL)
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
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
            LinkHearingDetails hearingInGroup = new LinkHearingDetails();
            hearingInGroup.setHearingId("2000000012");
            hearingInGroup.setHearingOrder(1);

            LinkHearingDetails hearingInGroup1 = new LinkHearingDetails();
            hearingInGroup1.setHearingId("2000000002");
            hearingInGroup1.setHearingOrder(1);

            HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
            GroupDetails groupDetails = generateGroupDetails(LinkType.ORDERED);
            hearingLinkGroupRequest.setGroupDetails(groupDetails);
            hearingLinkGroupRequest.setHearingsInGroup(Arrays.asList(hearingInGroup, hearingInGroup1));

            mockMvc.perform(post(URL)
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
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
            LinkHearingDetails hearingInGroup = new LinkHearingDetails();
            hearingInGroup.setHearingId("2000000012");
            hearingInGroup.setHearingOrder(1);

            LinkHearingDetails hearingInGroup1 = new LinkHearingDetails();
            hearingInGroup1.setHearingId("2000000013");
            hearingInGroup1.setHearingOrder(2);

            HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
            GroupDetails groupDetails = generateGroupDetails(LinkType.SAME_SLOT);
            hearingLinkGroupRequest.setGroupDetails(groupDetails);
            hearingLinkGroupRequest.setHearingsInGroup(Arrays.asList(hearingInGroup, hearingInGroup1));

            stubPostCreateLinkHearingGroup(200, HMI_REQUEST_URL, TOKEN);

            mockMvc.perform(post(URL)
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(hearingLinkGroupRequest)))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.hearingGroupRequestId").exists())
                .andReturn();
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
        void shouldReturn400_WhenReceiving4xxFromListAssist() throws Exception {
            LinkHearingDetails hearingInGroup = new LinkHearingDetails();
            hearingInGroup.setHearingId("2000000012");
            hearingInGroup.setHearingOrder(1);

            LinkHearingDetails hearingInGroup1 = new LinkHearingDetails();
            hearingInGroup1.setHearingId("2000000013");
            hearingInGroup1.setHearingOrder(2);

            HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
            GroupDetails groupDetails = generateGroupDetails(LinkType.ORDERED);
            hearingLinkGroupRequest.setGroupDetails(groupDetails);
            hearingLinkGroupRequest.setHearingsInGroup(Arrays.asList(hearingInGroup, hearingInGroup1));

            stubPostCreateLinkHearingGroup(400, HMI_REQUEST_URL, TOKEN);
            mockMvc.perform(post(URL)
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(hearingLinkGroupRequest)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasItem(REJECTED_BY_LIST_ASSIST)))
                .andReturn();
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
        void shouldReturn400_WhenReceiving5xxFromListAssist() throws Exception {
            LinkHearingDetails hearingInGroup = new LinkHearingDetails();
            hearingInGroup.setHearingId("2000000012");
            hearingInGroup.setHearingOrder(1);

            LinkHearingDetails hearingInGroup1 = new LinkHearingDetails();
            hearingInGroup1.setHearingId("2000000013");
            hearingInGroup1.setHearingOrder(2);

            HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
            GroupDetails groupDetails = generateGroupDetails(LinkType.SAME_SLOT);
            hearingLinkGroupRequest.setGroupDetails(groupDetails);
            hearingLinkGroupRequest.setHearingsInGroup(Arrays.asList(hearingInGroup, hearingInGroup1));

            stubPostCreateLinkHearingGroup(500, HMI_REQUEST_URL, TOKEN);
            mockMvc.perform(post(URL)
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(hearingLinkGroupRequest)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasItem(LIST_ASSIST_FAILED_TO_RESPOND)))
                .andReturn();
        }
    }

    @Nested
    @DisplayName("PutLinkedHearingGroup")
    class PutLinkedHearingGroup {
        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
        void shouldReturn400_WhenThereIsOnlyOneHearing() throws Exception {
            LinkHearingDetails hearingInGroup = new LinkHearingDetails();
            hearingInGroup.setHearingId("2000000001");
            hearingInGroup.setHearingOrder(1);

            HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
            GroupDetails groupDetails = generateGroupDetails(LinkType.SAME_SLOT);
            hearingLinkGroupRequest.setGroupDetails(groupDetails);
            hearingLinkGroupRequest.setHearingsInGroup(Arrays.asList(hearingInGroup));

            mockMvc.perform(put(URL + "?id=2")
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingLinkGroupRequest)))
                    .andExpect(status().is(400))
                    .andExpect(jsonPath("$.errors", hasItem(ValidationError.HEARINGS_IN_GROUP_SIZE
                    )))
                    .andReturn();
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
        void shouldReturn400_WhenHearingIsNotUniqueInGroup() throws Exception {
            LinkHearingDetails hearingInGroup = new LinkHearingDetails();
            hearingInGroup.setHearingId("2000000000");
            hearingInGroup.setHearingOrder(1);

            HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
            GroupDetails groupDetails = generateGroupDetails(LinkType.SAME_SLOT);
            hearingLinkGroupRequest.setGroupDetails(groupDetails);
            hearingLinkGroupRequest.setHearingsInGroup(Arrays.asList(hearingInGroup, hearingInGroup));

            mockMvc.perform(put(URL + "?id=12")
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(hearingLinkGroupRequest)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasItem(ValidationError.HEARINGS_IN_GROUP_SIZE
                )))
                .andReturn();

        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
        void shouldReturn404_WhenHearingDoesNotExist() throws Exception {
            LinkHearingDetails hearingInGroup = new LinkHearingDetails();
            hearingInGroup.setHearingId("2000000001");
            hearingInGroup.setHearingOrder(1);

            LinkHearingDetails hearingInGroup1 = new LinkHearingDetails();
            hearingInGroup1.setHearingId("2000000002");
            hearingInGroup1.setHearingOrder(1);

            HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
            GroupDetails groupDetails = generateGroupDetails(LinkType.SAME_SLOT);
            hearingLinkGroupRequest.setGroupDetails(groupDetails);
            hearingLinkGroupRequest.setHearingsInGroup(Arrays.asList(hearingInGroup, hearingInGroup1));

            mockMvc.perform(put(URL + "?id=12")
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(hearingLinkGroupRequest)))
                .andExpect(status().is(404))
                .andExpect(jsonPath("$.errors", hasItem("No hearing found for reference: 2000000001"
                )))
                .andReturn();

        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
        void shouldReturn400_WhenHearingIsMalformed() throws Exception {
            LinkHearingDetails hearingInGroup = new LinkHearingDetails();
            hearingInGroup.setHearingId("1000000001");
            hearingInGroup.setHearingOrder(1);

            LinkHearingDetails hearingInGroup1 = new LinkHearingDetails();
            hearingInGroup1.setHearingId("2000000002");
            hearingInGroup1.setHearingOrder(1);

            HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
            GroupDetails groupDetails = generateGroupDetails(LinkType.SAME_SLOT);
            hearingLinkGroupRequest.setGroupDetails(groupDetails);
            hearingLinkGroupRequest.setHearingsInGroup(Arrays.asList(hearingInGroup, hearingInGroup1));

            mockMvc.perform(put(URL + "?id=12")
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
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
            LinkHearingDetails hearingInGroup = new LinkHearingDetails();
            hearingInGroup.setHearingId("2000000000");
            hearingInGroup.setHearingOrder(1);

            LinkHearingDetails hearingInGroup1 = new LinkHearingDetails();
            hearingInGroup1.setHearingId("2000000002");
            hearingInGroup1.setHearingOrder(1);

            HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
            GroupDetails groupDetails = generateGroupDetails(LinkType.SAME_SLOT);
            hearingLinkGroupRequest.setGroupDetails(groupDetails);
            hearingLinkGroupRequest.setHearingsInGroup(Arrays.asList(hearingInGroup, hearingInGroup1));

            mockMvc.perform(put(URL + "?id=12")
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
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
            LinkHearingDetails hearingInGroup = new LinkHearingDetails();
            hearingInGroup.setHearingId("2000000005");
            hearingInGroup.setHearingOrder(1);

            LinkHearingDetails hearingInGroup1 = new LinkHearingDetails();
            hearingInGroup1.setHearingId("2000000008");
            hearingInGroup1.setHearingOrder(1);

            HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
            GroupDetails groupDetails = generateGroupDetails(LinkType.SAME_SLOT);
            hearingLinkGroupRequest.setGroupDetails(groupDetails);
            hearingLinkGroupRequest.setHearingsInGroup(Arrays.asList(hearingInGroup, hearingInGroup1));

            mockMvc.perform(put(URL + "?id=12")
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
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
            LinkHearingDetails hearingInGroup = new LinkHearingDetails();
            hearingInGroup.setHearingId("2000000011");
            hearingInGroup.setHearingOrder(1);

            LinkHearingDetails hearingInGroup1 = new LinkHearingDetails();
            hearingInGroup1.setHearingId("2000000000");
            hearingInGroup1.setHearingOrder(1);

            HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
            GroupDetails groupDetails = generateGroupDetails(LinkType.SAME_SLOT);
            hearingLinkGroupRequest.setGroupDetails(groupDetails);
            hearingLinkGroupRequest.setHearingsInGroup(Arrays.asList(hearingInGroup, hearingInGroup1));

            mockMvc.perform(post(URL)
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
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
            LinkHearingDetails hearingInGroup = new LinkHearingDetails();
            hearingInGroup.setHearingId("2000000012");
            hearingInGroup.setHearingOrder(1);

            LinkHearingDetails hearingInGroup1 = new LinkHearingDetails();
            hearingInGroup1.setHearingId("2000000013");
            hearingInGroup1.setHearingOrder(1);

            HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
            GroupDetails groupDetails = generateGroupDetails(LinkType.ORDERED);
            hearingLinkGroupRequest.setGroupDetails(groupDetails);
            hearingLinkGroupRequest.setHearingsInGroup(Arrays.asList(hearingInGroup, hearingInGroup1));

            mockMvc.perform(post(URL)
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(hearingLinkGroupRequest)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasItem("005 Hearing Order is not unique"
                )))
                .andReturn();
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
        void shouldReturn404_ForInvalidRequestId() throws Exception {
            LinkHearingDetails hearingInGroup1 = new LinkHearingDetails();
            hearingInGroup1.setHearingId("2000000007");
            hearingInGroup1.setHearingOrder(1);
            LinkHearingDetails hearingInGroup2 = new LinkHearingDetails();
            hearingInGroup2.setHearingId("2000000008");
            hearingInGroup2.setHearingOrder(2);

            HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
            GroupDetails groupDetails = generateGroupDetails(LinkType.SAME_SLOT);
            hearingLinkGroupRequest.setGroupDetails(groupDetails);
            hearingLinkGroupRequest.setHearingsInGroup(Arrays.asList(hearingInGroup1, hearingInGroup2));
            logger.info("json: {}", objectMapper.writeValueAsString(hearingLinkGroupRequest));

            mockMvc.perform(put(URL + "?id=88782")
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(hearingLinkGroupRequest)))
                .andExpect(status().is(404))
                .andExpect(jsonPath("$.errors", hasItem(ValidationError.INVALID_LINKED_GROUP_REQUEST_ID_DETAILS)))
                .andReturn();

        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
        void shouldReturn200_ForValidRequest() throws Exception {
            LinkHearingDetails hearingInGroup1 = new LinkHearingDetails();
            hearingInGroup1.setHearingId("2000000007");
            hearingInGroup1.setHearingOrder(1);
            LinkHearingDetails hearingInGroup2 = new LinkHearingDetails();
            hearingInGroup2.setHearingId("2000000008");
            hearingInGroup2.setHearingOrder(2);

            HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
            GroupDetails groupDetails = generateGroupDetails(LinkType.SAME_SLOT);
            hearingLinkGroupRequest.setGroupDetails(groupDetails);
            hearingLinkGroupRequest.setHearingsInGroup(Arrays.asList(
                hearingInGroup1, hearingInGroup2));

            logger.info(objectMapper.writeValueAsString(hearingLinkGroupRequest));
            stubPutUpdateLinkHearingGroup(200, "12", TOKEN);
            mockMvc.perform(put(URL + "?id=12")
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(hearingLinkGroupRequest)))
                .andExpect(status().is(200))
                .andReturn();
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
        void shouldReturn400_WhenReceiving4xxFromListAssist() throws Exception {
            LinkHearingDetails hearingInGroup1 = new LinkHearingDetails();
            hearingInGroup1.setHearingId("2000000007");
            hearingInGroup1.setHearingOrder(1);
            LinkHearingDetails hearingInGroup2 = new LinkHearingDetails();
            hearingInGroup2.setHearingId("2000000008");
            hearingInGroup2.setHearingOrder(2);

            HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
            GroupDetails groupDetails = generateGroupDetails(LinkType.SAME_SLOT);
            hearingLinkGroupRequest.setGroupDetails(groupDetails);
            hearingLinkGroupRequest.setHearingsInGroup(Arrays.asList(
                hearingInGroup1, hearingInGroup2));

            logger.info(objectMapper.writeValueAsString(hearingLinkGroupRequest));
            stubPutUpdateLinkHearingGroup(400, "12", TOKEN);
            mockMvc.perform(put(URL + "?id=12")
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(hearingLinkGroupRequest)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasItem(REJECTED_BY_LIST_ASSIST)))
                .andReturn();
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
        void shouldReturn400_WhenReceiving5xxFromListAssist() throws Exception {
            LinkHearingDetails hearingInGroup1 = new LinkHearingDetails();
            hearingInGroup1.setHearingId("2000000007");
            hearingInGroup1.setHearingOrder(1);
            LinkHearingDetails hearingInGroup2 = new LinkHearingDetails();
            hearingInGroup2.setHearingId("2000000008");
            hearingInGroup2.setHearingOrder(2);

            HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
            GroupDetails groupDetails = generateGroupDetails(LinkType.SAME_SLOT);
            hearingLinkGroupRequest.setGroupDetails(groupDetails);
            hearingLinkGroupRequest.setHearingsInGroup(Arrays.asList(
                hearingInGroup1, hearingInGroup2));

            logger.info(objectMapper.writeValueAsString(hearingLinkGroupRequest));
            stubPutUpdateLinkHearingGroup(500, "12", TOKEN);
            mockMvc.perform(put(URL + "?id=12")
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(hearingLinkGroupRequest)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasItem(LIST_ASSIST_FAILED_TO_RESPOND)))
                .andReturn();
        }
    }

    @Nested
    @DisplayName("DeleteLinkedHearingGroup")
    class DeleteLinkedHearingGroup {

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
        void shouldReturn404_WhenHearingGroupDoesNotExist() throws Exception {
            mockMvc.perform(delete(URL + "/11111")
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(404))
                .andReturn();
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
        void shouldReturn400_WhenHearingGroupStatusIsPending() throws Exception {
            mockMvc.perform(delete(URL + "/44445")
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(400))
                .andExpect(jsonPath(ERROR_PATH_ERROR).value("007 group is in a PENDING state"))
                .andReturn();
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
        void shouldReturn400_WhenHearingGroupStatusIsError() throws Exception {
            mockMvc.perform(delete(URL + "/44446")
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(400))
                .andExpect(jsonPath(ERROR_PATH_ERROR).value("007 group is in a ERROR state"))
                .andReturn();
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
        void shouldReturn400_WhenHearingGroupHearingResponseStartDateIsInThePastForHearingStatusHearingRequested()
            throws Exception {
            mockMvc.perform(delete(URL + "/44447")
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(400))
                .andExpect(jsonPath(ERROR_PATH_ERROR)
                               .value("008 Invalid state for unlinking hearing request 2000000301"))
                .andReturn();
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
        void shouldReturn400_WhenHearingGroupHearingResponseStartDateIsInThePastForHearingStatusUpdateRequested()
            throws Exception {
            mockMvc.perform(delete(URL + "/44448")
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(400))
                .andExpect(jsonPath(ERROR_PATH_ERROR)
                               .value("008 Invalid state for unlinking hearing request 2000000302"))
                .andReturn();
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
        void shouldReturn200_WhenHearingGroupExists() throws Exception {
            stubSuccessfullyDeleteLinkedHearingGroups(TOKEN, "12345");
            mockMvc.perform(delete(URL + "/12345")
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(200))
                .andReturn();
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
        void shouldReturn4xx_WhenDeleteLinkedHearing() throws Exception {
            stubDeleteLinkedHearingGroupsReturn4XX(TOKEN, "12345");
            mockMvc.perform(delete(URL + "/12345")
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasItem(REJECTED_BY_LIST_ASSIST)))
                .andReturn();
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
        void shouldReturn5xx_WhenDeleteLinkedHearing() throws Exception {
            stubDeleteLinkedHearingGroupsReturn5XX(TOKEN, "12345");
            mockMvc.perform(delete(URL + "/12345")
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasItem(LIST_ASSIST_FAILED_TO_RESPOND)))
                .andReturn();
        }
    }

    @Nested
    @DisplayName("GetLinkedHearingGroup")
    class GetLinkedHearingGroup {

        @Test
        void shouldReturn400_WhenLinkedHearingGroupIdIsNotValid() throws Exception {
            mockMvc.perform(get(URL + "/760000000o")
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(400))
                .andReturn();
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
        void shouldReturn200WhenLinkedHearingGroupExists() throws Exception {
            mockMvc.perform(get(URL + "/12")
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.groupDetails").exists())
                .andExpect(jsonPath("$.groupDetails.groupName").value("HMAN-56 group 2"))
                .andExpect(jsonPath("$.groupDetails.groupReason").value("Test 2"))
                .andExpect(jsonPath("$.groupDetails.groupLinkType").value("Ordered"))
                .andExpect(jsonPath("$.groupDetails.groupComments").value("commented"))
                .andExpect(jsonPath("$.hearingsInGroup",hasSize(3)))
                .andExpect(jsonPath("$.hearingsInGroup[0].hearingId").value("2000000007"))
                .andExpect(jsonPath("$.hearingsInGroup[0].hearingOrder").value("1"))
                .andExpect(jsonPath("$.hearingsInGroup[1].hearingId").value("2000000008"))
                .andExpect(jsonPath("$.hearingsInGroup[1].hearingOrder").value("2"))
                .andExpect(jsonPath("$.hearingsInGroup[2].hearingId").value("2000000009"))
                .andReturn();
        }
    }

    private GroupDetails generateGroupDetails(LinkType linkType) {
        GroupDetails groupDetails = new GroupDetails();
        groupDetails.setGroupComments("comments");
        groupDetails.setGroupLinkType(linkType.label);
        groupDetails.setGroupName("name");
        groupDetails.setGroupReason("reason");
        return groupDetails;
    }

    private final String serviceJwtXuiWeb = generateDummyS2SToken("xui_webapp");
}
