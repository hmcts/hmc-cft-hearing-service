package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.OrganisationDetailEntity;
import uk.gov.hmcts.reform.hmc.model.OrganisationDetails;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrganisationDetailMapperTest {

    @Test
    void modelToEntity() {
        OrganisationDetailMapper mapper = new OrganisationDetailMapper();
        HearingPartyEntity hearingPartyEntity = new HearingPartyEntity();
        OrganisationDetails organisationDetail = TestingUtil.organisationDetails();
        OrganisationDetailEntity entity = mapper.modelToEntity(organisationDetail, hearingPartyEntity);
        assertEquals("name", entity.getOrganisationName());
        assertEquals("type", entity.getOrganisationTypeCode());
        assertEquals("cft", entity.getHmctsOrganisationReference());
    }
}
