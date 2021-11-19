package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.PartyType;

@Component
public class HearingPartyMapper {

    public HearingPartyEntity modelToEntity(PartyDetails partyDetail,
                                                  CaseHearingRequestEntity caseHearingRequestEntity) {
        final HearingPartyEntity hearingPartyEntity = new HearingPartyEntity();
        hearingPartyEntity.setPartyReference(partyDetail.getPartyID());
        hearingPartyEntity.setPartyType(PartyType.valueOf(partyDetail.getPartyType()));
        hearingPartyEntity.setPartyRoleType(partyDetail.getPartyRole());
        hearingPartyEntity.setCaseHearing(caseHearingRequestEntity);
        return hearingPartyEntity;
    }
}
