package uk.gov.hmcts.reform.hmc.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.hmc.TestIdamConfiguration;
import uk.gov.hmcts.reform.hmc.config.SecurityConfiguration;
import uk.gov.hmcts.reform.hmc.domain.model.enums.LinkType;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.GroupDetails;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.LinkHearingDetails;
import uk.gov.hmcts.reform.hmc.security.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.hmc.service.LinkedHearingGroupService;
import uk.gov.hmcts.reform.hmc.service.common.DefaultObjectMapperService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = LinkHearingGroupController.class,
    excludeFilters = @ComponentScan.Filter(type = ASSIGNABLE_TYPE, classes =
        {SecurityConfiguration.class, JwtGrantedAuthoritiesConverter.class}))
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(TestIdamConfiguration.class)
class LinkHearingGroupControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(LinkHearingGroupControllerTest.class);

    private DefaultObjectMapperService objectMapperService;
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    WebApplicationContext webApplicationContext;

    @MockBean
    private LinkedHearingGroupService linkedHearingGroupService;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapperService = new DefaultObjectMapperService(objectMapper);
    }

    @Nested
    @DisplayName("validateHearingLink")
    class ValidateHearingLink {
        @Test
        void shouldReturn200_whenRequest_Details_Are_Present() {
            GroupDetails groupDetails = generateGroupDetails("comment", "name",
                                                             LinkType.ORDERED, "reason"
            );
            LinkHearingDetails hearingDetails1 = generateHearingDetails("2000000000", 1);
            LinkHearingDetails hearingDetails2 = generateHearingDetails("2000000002", 2);
            HearingLinkGroupRequest hearingLinkGroupRequest = generateHearingLink(
                groupDetails,
                Arrays.asList(
                    hearingDetails1,
                    hearingDetails2
                )
            );

            LinkHearingGroupController controller = new LinkHearingGroupController(linkedHearingGroupService);
            var jsonNode = objectMapperService.convertObjectToJsonNode(hearingLinkGroupRequest);
            logger.info("jsonNode: {}", jsonNode);
            controller.validateLinkHearing(hearingLinkGroupRequest);
            verify(linkedHearingGroupService, times(1)).linkHearing(hearingLinkGroupRequest);
        }

        @Test
        void shouldReturn400_whenRequest_HasOnlyOneHearingDetail() {
            GroupDetails groupDetails = generateGroupDetails("comment", "name",
                                                             LinkType.ORDERED, "reason"
            );
            LinkHearingDetails hearingDetails1 = generateHearingDetails("2000000000", 1);
            HearingLinkGroupRequest hearingLinkGroupRequest = generateHearingLink(
                groupDetails,
                Arrays.asList(
                    hearingDetails1
                )
            );

            LinkHearingGroupController controller = new LinkHearingGroupController(linkedHearingGroupService);
            controller.validateLinkHearing(hearingLinkGroupRequest);
            verify(linkedHearingGroupService, times(1)).linkHearing(hearingLinkGroupRequest);
        }
    }

    private HearingLinkGroupRequest generateHearingLink(GroupDetails groupDetails,
                                                        List<LinkHearingDetails> hearingDetails) {

        HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
        hearingLinkGroupRequest.setHearingsInGroup(hearingDetails);
        hearingLinkGroupRequest.setGroupDetails(groupDetails);

        return hearingLinkGroupRequest;
    }

    private GroupDetails generateGroupDetails(String groupComments, String groupName, LinkType linktype,
                                              String groupReason) {
        GroupDetails groupDetails = new GroupDetails();
        groupDetails.setGroupComments(groupComments);
        groupDetails.setGroupName(groupName);
        groupDetails.setGroupLinkType(linktype.label);
        groupDetails.setGroupReason(groupReason);
        return groupDetails;
    }

    private LinkHearingDetails generateHearingDetails(String hearingId, int order) {
        LinkHearingDetails hearingDetails = new LinkHearingDetails();
        hearingDetails.setHearingId(hearingId);
        hearingDetails.setHearingOrder(order);
        return hearingDetails;
    }

}
