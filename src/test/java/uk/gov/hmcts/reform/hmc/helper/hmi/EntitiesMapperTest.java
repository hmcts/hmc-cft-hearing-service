package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.hmc.model.IndividualDetails;
import uk.gov.hmcts.reform.hmc.model.OrganisationDetails;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.RelatedParty;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityDow;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityRanges;
import uk.gov.hmcts.reform.hmc.model.hmi.Entity;
import uk.gov.hmcts.reform.hmc.model.hmi.EntityCommunication;
import uk.gov.hmcts.reform.hmc.model.hmi.EntitySubType;
import uk.gov.hmcts.reform.hmc.model.hmi.EntityUnavailableDate;
import uk.gov.hmcts.reform.hmc.model.hmi.EntityUnavailableDay;
import uk.gov.hmcts.reform.hmc.model.hmi.RelatedEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.helper.hmi.EntitySubTypeMapper.ORGANISATION_CLASS_CODE;
import static uk.gov.hmcts.reform.hmc.helper.hmi.EntitySubTypeMapper.PERSON_CLASS_CODE;

@ExtendWith(MockitoExtension.class)
public class EntitiesMapperTest {

    @InjectMocks
    private EntitiesMapper entitiesMapper;

    @Mock
    private UnavailableDatesMapper unavailableDatesMapper;

    @Mock
    private UnavailableDaysMapper unavailableDaysMapper;

    @Mock
    private CommunicationsMapper communicationsMapper;

    @Mock
    private RelatedEntitiesMapper relatedEntitiesMapper;

    @Mock
    private EntitySubTypeMapper entitySubTypeMapper;

    private static final String PREFERRED_HEARING_CHANNEL = "PreferredHearingChannel";
    private static final String PREFERRED_HEARING_CHANNEL_TWO = "PreferredHearingChannelTwo";
    private static final String ADJUSTMENTS = "Adjustment";
    private static final String PARTY_ID = "PartyId";
    private static final String PARTY_TYPE = "PartyType";
    private static final String PARTY_ROLE = "PartyRole";
    private static final String PARTY_TYPE_TWO = "PartyTypeTwo";
    private static final String PARTY_ROLE_TWO = "PartyRoleTwo";
    private static final String PARTY_ID_TWO = "PartyIdTwo";
    private static final String ADJUSTMENTS_THREE = "AdjustmentThree";
    private static final String PARTY_TYPE_THREE = "PartyTypeThree";
    private static final String PARTY_ROLE_THREE = "PartyRoleThree";
    private static final String PARTY_ID_THREE = "PartyIdThree";

    @Test
    void shouldReturnEntities() {
        EntitySubType entitySubTypeOrg = EntitySubType.builder()
            .entityClassCode(ORGANISATION_CLASS_CODE)
            .build();
        EntitySubType entitySubTypePerson = EntitySubType.builder()
            .entityClassCode(PERSON_CLASS_CODE)
            .build();
        EntityUnavailableDate entityUnavailableDate =  EntityUnavailableDate.builder().build();
        EntityUnavailableDay entityUnavailableDay = EntityUnavailableDay.builder().build();
        EntityCommunication entityCommunication = EntityCommunication.builder().build();
        RelatedEntity relatedEntity = RelatedEntity.builder().build();
        when(entitySubTypeMapper.getPersonEntitySubType(any())).thenReturn(entitySubTypePerson);
        when(entitySubTypeMapper.getOrgEntitySubType(any())).thenReturn(entitySubTypeOrg);
        when(unavailableDatesMapper.getUnavailableDates(any())).thenReturn(Collections
                                                                               .singletonList(entityUnavailableDate));
        when(communicationsMapper.getCommunications(any())).thenReturn(Collections.singletonList(entityCommunication));
        when(unavailableDaysMapper.getUnavailableDays(any())).thenReturn(Collections
                                                                             .singletonList(entityUnavailableDay));
        when(relatedEntitiesMapper.getRelatedEntities(any())).thenReturn(Collections.singletonList(relatedEntity));

        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setPreferredHearingChannel(PREFERRED_HEARING_CHANNEL);
        individualDetails.setReasonableAdjustments(Collections.singletonList(ADJUSTMENTS));
        RelatedParty relatedParty = new RelatedParty();
        individualDetails.setRelatedParties(Collections.singletonList(relatedParty));
        PartyDetails partyDetailsOne = new PartyDetails();
        partyDetailsOne.setIndividualDetails(individualDetails);
        UnavailabilityRanges unavailabilityRanges = new UnavailabilityRanges();
        partyDetailsOne.setUnavailabilityRanges(Collections.singletonList(unavailabilityRanges));
        UnavailabilityDow unavailabilityDow = new UnavailabilityDow();
        partyDetailsOne.setUnavailabilityDow(Collections.singletonList(unavailabilityDow));
        partyDetailsOne.setPartyID(PARTY_ID);
        partyDetailsOne.setPartyType(PARTY_TYPE);
        partyDetailsOne.setPartyRole(PARTY_ROLE);

        PartyDetails partyDetailsTwo = new PartyDetails();
        OrganisationDetails organisationDetails = new OrganisationDetails();
        partyDetailsTwo.setOrganisationDetails(organisationDetails);
        partyDetailsTwo.setPartyID(PARTY_ID_TWO);
        partyDetailsTwo.setPartyType(PARTY_TYPE_TWO);
        partyDetailsTwo.setPartyRole(PARTY_ROLE_TWO);

        IndividualDetails individualDetailsTwo = new IndividualDetails();
        individualDetailsTwo.setPreferredHearingChannel(PREFERRED_HEARING_CHANNEL);
        individualDetailsTwo.setReasonableAdjustments(Collections.singletonList(ADJUSTMENTS_THREE));
        RelatedParty relatedPartyTwo = new RelatedParty();
        individualDetailsTwo.setRelatedParties(Collections.singletonList(relatedPartyTwo));
        PartyDetails partyDetailsThree = new PartyDetails();
        partyDetailsThree.setIndividualDetails(individualDetailsTwo);
        partyDetailsThree.setPartyID(PARTY_ID_THREE);
        partyDetailsThree.setPartyType(PARTY_TYPE_THREE);
        partyDetailsThree.setPartyRole(PARTY_ROLE_THREE);

        IndividualDetails individualDetailsFour = new IndividualDetails();
        individualDetailsFour.setPreferredHearingChannel(null);
        individualDetailsFour.setReasonableAdjustments(Collections.singletonList(ADJUSTMENTS_THREE));
        RelatedParty relatedPartyFour = new RelatedParty();
        individualDetailsFour.setRelatedParties(Collections.singletonList(relatedPartyFour));
        PartyDetails partyDetailsFour = new PartyDetails();
        partyDetailsFour.setIndividualDetails(individualDetailsFour);

        IndividualDetails individualDetailsFive = new IndividualDetails();
        individualDetailsFive.setPreferredHearingChannel(PREFERRED_HEARING_CHANNEL_TWO);
        individualDetailsFive.setReasonableAdjustments(Collections.singletonList(ADJUSTMENTS_THREE));
        RelatedParty relatedPartyFive = new RelatedParty();
        individualDetailsFive.setRelatedParties(Collections.singletonList(relatedPartyFive));
        PartyDetails partyDetailsFive = new PartyDetails();
        partyDetailsFive.setIndividualDetails(individualDetailsFive);

        List<PartyDetails> partyDetails = Arrays.asList(partyDetailsOne, partyDetailsTwo, partyDetailsThree,
                                                        partyDetailsFour, partyDetailsFive);
        EntitiesMapperObject actualEntitiesMapperObject = entitiesMapper.getEntities(partyDetails);
        assertTrue(actualEntitiesMapperObject.getPreferredHearingChannels().contains(PREFERRED_HEARING_CHANNEL));
        assertTrue(actualEntitiesMapperObject.getPreferredHearingChannels().contains(PREFERRED_HEARING_CHANNEL_TWO));
        assertEquals(2, actualEntitiesMapperObject.getPreferredHearingChannels().size());

        List<Entity> entities = actualEntitiesMapperObject.getEntities();
        assertEquals(PARTY_ID, entities.get(0).getEntityId());
        assertEquals(PARTY_TYPE, entities.get(0).getEntityTypeCode());
        assertEquals(PARTY_ROLE, entities.get(0).getEntityRoleCode());
        assertEquals(entitySubTypePerson, entities.get(0).getEntitySubType());
        assertEquals(PREFERRED_HEARING_CHANNEL, entities.get(0).getEntityHearingChannel());
        assertEquals(entityCommunication, entities.get(0).getEntityCommunications().get(0));
        assertEquals(ADJUSTMENTS, entities.get(0).getEntitySpecialMeasures().get(0));
        assertEquals(entityUnavailableDay, entities.get(0).getEntityUnavailableDays().get(0));
        assertEquals(entityUnavailableDate, entities.get(0).getEntityUnavailableDates().get(0));
        assertEquals(relatedEntity, entities.get(0).getEntityRelatedEntities().get(0));

        assertEquals(PARTY_ID_TWO, entities.get(1).getEntityId());
        assertEquals(PARTY_TYPE_TWO, entities.get(1).getEntityTypeCode());
        assertEquals(PARTY_ROLE_TWO, entities.get(1).getEntityRoleCode());
        assertEquals(entitySubTypeOrg, entities.get(1).getEntitySubType());

        assertEquals(5, entities.size());
    }
}
