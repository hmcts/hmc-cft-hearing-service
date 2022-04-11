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
            EntityCommunication entityCommunication = EntityCommunication.builder()
                .entityCommunicationDetails(partyDetails.getIndividualDetails().getHearingChannelEmail())
                .entityCommunicationType(EMAIL_TYPE)
                .build();
            entityCommunications.add(entityCommunication);
        }
        if (partyDetails.getIndividualDetails().getHearingChannelPhone() != null) {
            EntityCommunication entityCommunication = EntityCommunication.builder()
                .entityCommunicationDetails(partyDetails.getIndividualDetails().getHearingChannelPhone())
                .entityCommunicationType(PHONE_TYPE)
                .build();
            entityCommunications.add(entityCommunication);
        }
        return entityCommunications;
    }

}
