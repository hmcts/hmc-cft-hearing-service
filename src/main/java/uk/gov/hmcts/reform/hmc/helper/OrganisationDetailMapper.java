package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.OrganisationDetailEntity;
import uk.gov.hmcts.reform.hmc.model.OrganisationDetails;

@Component
public class OrganisationDetailMapper {

    public OrganisationDetailEntity modelToEntity(OrganisationDetails organisationDetail,
                                                  HearingPartyEntity hearingPartyEntity) {
        final OrganisationDetailEntity organisationDetailEntity = new OrganisationDetailEntity();
        organisationDetailEntity.setOrganisationName(organisationDetail.getName());
        organisationDetailEntity.setOrganisationTypeCode(organisationDetail.getOrganisationType());
        organisationDetailEntity.setHmctsOrganisationReference(organisationDetail.getCftOrganisationID());
        organisationDetailEntity.setHearingParty(hearingPartyEntity);
        return organisationDetailEntity;

    }
}
