package uk.gov.hmcts.reform.hmc.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingDetailsAudit;
import uk.gov.hmcts.reform.hmc.domain.model.enums.LinkType;
import uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class LinkedHearingDetailsRepositoryTest {

    private static final Logger logger = LoggerFactory.getLogger(LinkedHearingDetailsRepositoryTest.class);

    @Mock
    private LinkedHearingDetailsRepository linkedHearingDetailsRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetLinkedHearingDetails() {
        Long hearingId = 2000000000L;
        Long groupId = 256L;
        Long order = 5L;
        LinkedHearingDetailsAudit lhdExpected =
                generateLinkedHearingDetails(1L, groupId, hearingId, order);

        logger.info("lhdExpected: {}", lhdExpected);

        doReturn(lhdExpected).when(linkedHearingDetailsRepository).getLinkedHearingDetailsByHearingId(any());
        LinkedHearingDetailsAudit lhd =
                linkedHearingDetailsRepository.getLinkedHearingDetailsByHearingId(hearingId);
        assertAll(
                () -> assertThat(lhd.getLinkedHearingDetailsAuditId(), is(1L)),
                () -> assertThat(lhd.getHearing().getId(), is(hearingId)),
                () -> assertThat(lhd.getLinkedGroup().getLinkedGroupId(), is(groupId)),
                () -> verify(linkedHearingDetailsRepository,
                        times(1)).getLinkedHearingDetailsByHearingId(any())
        );
    }

    private LinkedHearingDetailsAudit generateLinkedHearingDetails(Long lhdId, Long lgdId, Long hearingId,
                                                                   Long linkedOrder) {

        LinkedHearingDetailsAudit linkedHearingDetails = new LinkedHearingDetailsAudit();
        linkedHearingDetails.setHearing(generateHearing(hearingId));
        linkedHearingDetails.setLinkedHearingDetailsAuditId(lhdId);
        linkedHearingDetails.setLinkedGroup(generateLinkedGroupDetails(lgdId, "request1", "test request 1"));
        linkedHearingDetails.setLinkedOrder(linkedOrder);
        return linkedHearingDetails;
    }

    private HearingEntity generateHearing(Long id) {
        HearingEntity hearing = new HearingEntity();
        hearing.setId(id);
        hearing.setStatus(PutHearingStatus.HEARING_REQUESTED.name());
        hearing.setHearingResponses(new ArrayList<>());
        return hearing;
    }

    private LinkedGroupDetails generateLinkedGroupDetails(Long lgdId, String requestId, String requestName) {
        LinkedGroupDetails groupDetails = new LinkedGroupDetails();
        groupDetails.setLinkedGroupId(lgdId);
        groupDetails.setLinkType(LinkType.ORDERED);
        groupDetails.setReasonForLink("reason for link");
        groupDetails.setRequestDateTime(LocalDateTime.now());
        groupDetails.setRequestId(requestId);
        groupDetails.setRequestName(requestName);
        groupDetails.setStatus(PutHearingStatus.HEARING_REQUESTED.name());
        return groupDetails;
    }

}
