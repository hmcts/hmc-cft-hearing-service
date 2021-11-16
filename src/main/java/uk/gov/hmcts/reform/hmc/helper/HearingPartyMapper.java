package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.PartyType;

import java.util.ArrayList;
import java.util.List;

@Component
public class HearingPartyMapper {

    public HearingPartyMapper() {
    }

    public List<HearingPartyEntity> modelToEntity(List<PartyDetails> partyDetails,
                                                  CaseHearingRequestEntity caseHearingRequestEntity) {
        List<HearingPartyEntity> hearingPartyEntities = new ArrayList<>();
        for(PartyDetails partyDetail : partyDetails) {
            final HearingPartyEntity hearingPartyEntity = new HearingPartyEntity();
            hearingPartyEntity.setPartyReference(partyDetail.getPartyID());
            System.out.println("************** "+PartyType.valueOf(partyDetail.getPartyType()));
            hearingPartyEntity.setPartyType(PartyType.valueOf(partyDetail.getPartyType()));
            hearingPartyEntity.setPartyRoleType(partyDetail.getPartyRole());
            hearingPartyEntity.setCaseHearing(caseHearingRequestEntity);
            hearingPartyEntities.add(hearingPartyEntity);
        }
        return hearingPartyEntities;
    }
}
