package uk.gov.hmcts.reform.hmc.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingDetailsAudit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class LinkedHearingDetailsAuditRepositoryTest {

    @Mock
    private LinkedHearingDetailsAuditRepository linkedHearingDetailsAuditRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveLinkedHearingDetailsAudit() {
        LinkedGroupDetails groupDetails = createGroupDetailsEntity(2L, "ACTIVE");
        LinkedHearingDetailsAudit entity = new LinkedHearingDetailsAudit();
        entity.setLinkedOrder(1L);
        entity.setLinkedGroupVersion(10L);
        entity.setLinkedGroup(groupDetails);
        linkedHearingDetailsAuditRepository.save(entity);
        verify(linkedHearingDetailsAuditRepository, times(1)).save(any());
    }

    private LinkedGroupDetails createGroupDetailsEntity(Long hearingGroupId, String groupStatus) {
        LinkedGroupDetails groupDetails = new LinkedGroupDetails();
        groupDetails.setLinkedGroupId(hearingGroupId);
        groupDetails.setStatus(groupStatus);
        groupDetails.setLinkedGroupLatestVersion(1L);
        return groupDetails;
    }


}
