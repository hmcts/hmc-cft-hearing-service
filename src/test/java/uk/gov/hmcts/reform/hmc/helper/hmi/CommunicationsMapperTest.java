package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.model.IndividualDetails;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.hmi.EntityCommunication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.hmc.constants.Constants.EMAIL_TYPE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.PHONE_TYPE;

class CommunicationsMapperTest {

    @Test
    void shouldMapWhenPartyDetailsContainsEmailAndPhone() {
        PartyDetails partyDetails = new PartyDetails();
        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setHearingChannelPhone(List.of("phoneNumber"));
        individualDetails.setHearingChannelEmail(List.of("email"));
        partyDetails.setIndividualDetails(individualDetails);
        EntityCommunication entityCommunication = EntityCommunication.builder()
            .entityCommunicationType(PHONE_TYPE)
            .entityCommunicationDetails("phoneNumber")
            .build();
        EntityCommunication entityCommunicationTwo = EntityCommunication.builder()
            .entityCommunicationType(EMAIL_TYPE)
            .entityCommunicationDetails("email")
            .build();
        List<EntityCommunication> expectedCommunications = Arrays.asList(entityCommunicationTwo, entityCommunication);
        CommunicationsMapper communicationsMapper = new CommunicationsMapper();
        List<EntityCommunication> actualCommunications = communicationsMapper.getCommunications(partyDetails);
        assertEquals(expectedCommunications, actualCommunications);
    }

    @Test
    void shouldMapWhenPartyDetailsContainsMultipleEmailAndPhone() {
        PartyDetails partyDetails = new PartyDetails();
        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setHearingChannelPhone(List.of("phoneNumber1", "phoneNumber2"));
        individualDetails.setHearingChannelEmail(List.of("email1", "email2"));
        partyDetails.setIndividualDetails(individualDetails);
        EntityCommunication entityCommunicationPhoneOne = EntityCommunication.builder()
            .entityCommunicationType(PHONE_TYPE)
            .entityCommunicationDetails("phoneNumber1")
            .build();
        EntityCommunication entityCommunicationPhoneTwo = EntityCommunication.builder()
            .entityCommunicationType(PHONE_TYPE)
            .entityCommunicationDetails("phoneNumber2")
            .build();
        EntityCommunication entityCommunicationEmailOne = EntityCommunication.builder()
            .entityCommunicationType(EMAIL_TYPE)
            .entityCommunicationDetails("email1")
            .build();
        EntityCommunication entityCommunicationEmailTwo = EntityCommunication.builder()
            .entityCommunicationType(EMAIL_TYPE)
            .entityCommunicationDetails("email2")
            .build();
        List<EntityCommunication> expectedCommunications = Arrays.asList(
                                                                entityCommunicationEmailOne,
                                                                entityCommunicationEmailTwo,
                                                                entityCommunicationPhoneOne,
                                                                entityCommunicationPhoneTwo);
        CommunicationsMapper communicationsMapper = new CommunicationsMapper();
        List<EntityCommunication> actualCommunications = communicationsMapper.getCommunications(partyDetails);
        assertEquals(expectedCommunications, actualCommunications);
    }

    @Test
    void shouldMapWhenPartyDetailsLacksBothEmailAndPhone() {
        PartyDetails partyDetails = new PartyDetails();
        IndividualDetails individualDetails = new IndividualDetails();
        partyDetails.setIndividualDetails(individualDetails);
        CommunicationsMapper communicationsMapper = new CommunicationsMapper();
        List<EntityCommunication> actualCommunications = communicationsMapper.getCommunications(partyDetails);
        List<EntityCommunication> expectedCommunications = new ArrayList<>();
        assertEquals(expectedCommunications, actualCommunications);
    }

}
