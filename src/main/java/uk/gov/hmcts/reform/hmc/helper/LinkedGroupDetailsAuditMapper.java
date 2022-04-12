package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetailsAudit;

@Component
public class LinkedGroupDetailsAuditMapper {

    public LinkedGroupDetailsAudit modelToEntity(LinkedGroupDetails linkedGroupDetails) {
        LinkedGroupDetailsAudit linkedGroupDetailsAudit = new LinkedGroupDetailsAudit();
        linkedGroupDetailsAudit.setLinkedGroup(linkedGroupDetails);
        linkedGroupDetailsAudit.setLinkedGroupVersion(linkedGroupDetails.getLinkedGroupLatestVersion());
        linkedGroupDetailsAudit.setRequestId(linkedGroupDetails.getRequestId());
        linkedGroupDetailsAudit.setRequestName(linkedGroupDetails.getRequestName());
        linkedGroupDetailsAudit.setRequestDateTime(linkedGroupDetails.getRequestDateTime());
        linkedGroupDetailsAudit.setLinkType(linkedGroupDetails.getLinkType());
        linkedGroupDetailsAudit.setReasonForLink(linkedGroupDetails.getReasonForLink());
        linkedGroupDetailsAudit.setStatus(linkedGroupDetails.getStatus());
        linkedGroupDetailsAudit.setLinkedComments(linkedGroupDetails.getLinkedComments());
        return linkedGroupDetailsAudit;
    }
}
