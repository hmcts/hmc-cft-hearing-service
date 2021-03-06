package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingDetailsAudit;

@Component
public class LinkedHearingDetailsAuditMapper {

    public LinkedHearingDetailsAudit modelToEntity(HearingEntity hearingEntity) {
        LinkedGroupDetails linkedGroupDetails = new LinkedGroupDetails();
        linkedGroupDetails.setLinkedGroupId(hearingEntity.getLinkedGroupDetails().getLinkedGroupId());
        LinkedHearingDetailsAudit linkedHearingDetailsAuditEntity = new LinkedHearingDetailsAudit();
        linkedHearingDetailsAuditEntity.setLinkedGroup(linkedGroupDetails);
        linkedHearingDetailsAuditEntity.setLinkedGroupVersion(hearingEntity.getLinkedGroupDetails()
                                                                  .getLinkedGroupLatestVersion());
        linkedHearingDetailsAuditEntity.setHearing(hearingEntity);
        linkedHearingDetailsAuditEntity.setLinkedOrder(hearingEntity.getLinkedOrder());
        linkedHearingDetailsAuditEntity.getHearing().setLinkedGroupDetails(null);
        linkedHearingDetailsAuditEntity.getHearing().setLinkedOrder(null);
        return linkedHearingDetailsAuditEntity;
    }

    public LinkedHearingDetailsAudit modelToEntityUpdate(HearingEntity hearingEntity) {
        LinkedGroupDetails linkedGroupDetails = new LinkedGroupDetails();
        linkedGroupDetails.setLinkedGroupId(hearingEntity.getLinkedGroupDetails().getLinkedGroupId());
        LinkedHearingDetailsAudit linkedHearingDetailsAuditEntity = new LinkedHearingDetailsAudit();
        linkedHearingDetailsAuditEntity.setLinkedGroup(linkedGroupDetails);
        linkedHearingDetailsAuditEntity.setLinkedGroupVersion(hearingEntity.getLinkedGroupDetails()
                                                                  .getLinkedGroupLatestVersion());
        linkedHearingDetailsAuditEntity.setHearing(hearingEntity);
        linkedHearingDetailsAuditEntity.setLinkedOrder(hearingEntity.getLinkedOrder());
        return linkedHearingDetailsAuditEntity;
    }
}
