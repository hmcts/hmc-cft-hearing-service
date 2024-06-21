package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingDetailsAudit;

@Component
public class LinkedHearingDetailsAuditMapper {

    public LinkedHearingDetailsAudit modelToEntity(HearingEntity hearingEntity, LinkedGroupDetails linkedGroupDetails) {
        linkedGroupDetails.setLinkedGroupId(linkedGroupDetails.getLinkedGroupId());
        LinkedHearingDetailsAudit linkedHearingDetailsAuditEntity = new LinkedHearingDetailsAudit();
        linkedHearingDetailsAuditEntity.setLinkedGroup(linkedGroupDetails);
        linkedHearingDetailsAuditEntity.setLinkedGroupVersion(linkedGroupDetails.getLinkedGroupLatestVersion());
        linkedHearingDetailsAuditEntity.setHearing(hearingEntity);
        linkedHearingDetailsAuditEntity.setLinkedOrder(hearingEntity.getLinkedOrder());
        return linkedHearingDetailsAuditEntity;
    }
}
