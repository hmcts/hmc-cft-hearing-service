package uk.gov.hmcts.reform.hmc.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.domain.model.enums.LinkType;
import uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class LinkedGroupDetailsRepositoryTest {

    private static final Logger logger = LoggerFactory.getLogger(LinkedGroupDetailsRepositoryTest.class);

    @Mock
    private LinkedGroupDetailsRepository linkedGroupDetailsRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetLinkedGroupDetails() {
        String requestId = "1A";
        String requestName = "Test request";
        LinkedGroupDetails lgdExpected =
                generateLinkedGroupDetails(1L, requestId, requestName);

        doReturn(lgdExpected).when(linkedGroupDetailsRepository).getLinkedGroupDetailsByRequestId(any());
        LinkedGroupDetails lgd =
                linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(requestId);
        logger.info("lgd: {}", lgd);
        assertAll(
                () -> assertThat(lgd.getLinkedGroupId(), is(1L)),
                () -> assertThat(lgd.getRequestId(), is(requestId)),
                () -> assertThat(lgd.getRequestName(), is(requestName)),
                () -> verify(linkedGroupDetailsRepository,
                        times(1)).getLinkedGroupDetailsByRequestId(any())
        );
    }

    private LinkedGroupDetails generateLinkedGroupDetails(Long lgdId, String requestId, String requestName) {
        LinkedGroupDetails groupDetails = new LinkedGroupDetails();
        groupDetails.setLinkedGroupId(lgdId);
        groupDetails.setLinkType(LinkType.ORDERED.toString());
        groupDetails.setReasonForLink("reason for link");
        groupDetails.setRequestDateTime(LocalDateTime.now());
        groupDetails.setRequestId(requestId);
        groupDetails.setRequestName(requestName);
        groupDetails.setStatus(PutHearingStatus.HEARING_REQUESTED.name());
        return groupDetails;
    }

}
