package uk.gov.hmcts.reform.hmc.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetailsAudit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.LinkType.ORDERED;

class LinkedGroupDetailsAuditRepositoryTest {

    @Mock
    private LinkedGroupDetailsAuditRepository linkedGroupDetailsAuditRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveLinkedGroupDetailsAudit() {
        LinkedGroupDetails groupDetails = createGroupDetailsEntity(2L, "ACTIVE");
        LinkedGroupDetailsAudit groupDetailsAudit = new LinkedGroupDetailsAudit();
        groupDetailsAudit.setLinkedGroup(groupDetails);
        groupDetailsAudit.setLinkedGroupVersion(1L);
        groupDetailsAudit.setLinkType(ORDERED);
        groupDetailsAudit.setStatus("ACTIVE");
        linkedGroupDetailsAuditRepository.save(groupDetailsAudit);
        verify(linkedGroupDetailsAuditRepository, times(1)).save(any());
    }

    private LinkedGroupDetails createGroupDetailsEntity(Long hearingGroupId, String groupStatus) {
        LinkedGroupDetails groupDetails = new LinkedGroupDetails();
        groupDetails.setLinkedGroupId(hearingGroupId);
        groupDetails.setStatus(groupStatus);
        groupDetails.setLinkedGroupLatestVersion(1L);
        return groupDetails;
    }
}
