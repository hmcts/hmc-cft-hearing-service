package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.hmi.EntityCommunication;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.hmc.constants.Constants.EMAIL_TYPE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.PHONE_TYPE;

@Component
public class CommunicationsMapper {

    public List<EntityCommunication> getCommunications(PartyDetails partyDetails) {
        List<EntityCommunication> entityCommunications = new ArrayList<>();
        if (partyDetails.getIndividualDetails().getHearingChannelEmail() != null) {
            for (String email : partyDetails.getIndividualDetails().getHearingChannelEmail()) {
                EntityCommunication entityCommunication = EntityCommunication.builder()
                    .entityCommunicationDetails(email)
                    .entityCommunicationType(EMAIL_TYPE)
                    .build();
                entityCommunications.add(entityCommunication);
            }
        }
        if (partyDetails.getIndividualDetails().getHearingChannelPhone() != null) {
            for (String phoneNumber : partyDetails.getIndividualDetails().getHearingChannelPhone()) {
                EntityCommunication entityCommunication = EntityCommunication.builder()
                    .entityCommunicationDetails(phoneNumber)
                    .entityCommunicationType(PHONE_TYPE)
                    .build();
                entityCommunications.add(entityCommunication);
            }
        }
        return entityCommunications;
    }

}
