package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.PartyType;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HearingPartyMapperTest {

    @Test
    void modelToEntity() {
        HearingPartyMapper mapper = new HearingPartyMapper();
        CaseHearingRequestEntity caseHearingRequestEntity = new CaseHearingRequestEntity();
        PartyDetails partyDetail = TestingUtil.partyDetails().get(0);
        HearingPartyEntity entity = mapper.modelToEntity(partyDetail, caseHearingRequestEntity);
        assertEquals("P1", entity.getPartyReference());
        assertEquals(PartyType.IND, entity.getPartyType());
        assertEquals("DEF", entity.getPartyRoleType());
    }
}
