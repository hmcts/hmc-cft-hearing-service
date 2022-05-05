package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.PartyRelationshipDetailsEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PartyRelationshipDetailsMapperTest {

    private final PartyRelationshipDetailsMapper partyRelationshipDetailsMapper = new PartyRelationshipDetailsMapper();

    @Test
    void modelToEntity_NullRelatedParties() {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setIndividualDetails(TestingUtil.individualWithoutRelatedPartyDetails());
        assertTrue(partyRelationshipDetailsMapper.modelToEntity(partyDetails, Collections.emptyList()).isEmpty());
    }

    @Test
    void modelToEntity_PartyDetailsPartyId_DontMatch_hearingPartyReference() {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID("P1");
        partyDetails.setIndividualDetails(TestingUtil.individualDetails());
        HearingPartyEntity hearingPartyEntity = new HearingPartyEntity();
        hearingPartyEntity.setPartyReference("MyUnknownValue");
        final BadRequestException badRequestException = assertThrows(BadRequestException.class, () ->
                partyRelationshipDetailsMapper.modelToEntity(partyDetails, List.of(hearingPartyEntity)));
        assertEquals("Cannot find unique PartyID with value P1", badRequestException.getMessage());
    }

    @Test
    void modelToEntity_RelatedPartyIds_DontMatch_hearingPartyReference() {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID("P3");
        partyDetails.setIndividualDetails(TestingUtil.individualDetails());
        HearingPartyEntity hearingPartyEntity = new HearingPartyEntity();
        hearingPartyEntity.setPartyReference("P3");
        final BadRequestException badRequestException = assertThrows(BadRequestException.class, () ->
                partyRelationshipDetailsMapper.modelToEntity(partyDetails, List.of(hearingPartyEntity)));
        assertEquals("Cannot find unique PartyID with value P1", badRequestException.getMessage());
    }

    @Test
    void modelToEntity_ReturnsListOfPartyRelationshipDetailsEntities() {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID("P1");
        partyDetails.setIndividualDetails(TestingUtil.individualDetails());
        HearingPartyEntity sourceHearingPartyEntity = new HearingPartyEntity();
        sourceHearingPartyEntity.setPartyReference("P1");

        HearingPartyEntity targetHearingPartyEntity = new HearingPartyEntity();
        targetHearingPartyEntity.setPartyReference("P2");

        final List<PartyRelationshipDetailsEntity> partyRelationshipDetailsEntities =
                partyRelationshipDetailsMapper.modelToEntity(partyDetails,
                        List.of(sourceHearingPartyEntity, targetHearingPartyEntity));

        assertNotNull(partyRelationshipDetailsEntities);
        assertEquals(2, partyRelationshipDetailsEntities.size());
    }
}