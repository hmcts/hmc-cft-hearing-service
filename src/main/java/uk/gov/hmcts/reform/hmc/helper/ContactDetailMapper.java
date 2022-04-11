package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.ContactDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.model.IndividualDetails;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.hmc.constants.Constants.EMAIL_TYPE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.PHONE_TYPE;

@Component
public class ContactDetailMapper {

    public List<ContactDetailsEntity> modelToEntity(IndividualDetails individualDetails,
                                                    HearingPartyEntity hearingPartyEntity) {
        List<ContactDetailsEntity> contactDetailsEntities = new ArrayList<>();
        if (individualDetails.getHearingChannelEmail() != null) {
            final ContactDetailsEntity contactDetailsEntity = new ContactDetailsEntity();
            contactDetailsEntity.setContactDetails(individualDetails.getHearingChannelEmail());
            contactDetailsEntity.setContactType(EMAIL_TYPE);
            contactDetailsEntity.setHearingParty(hearingPartyEntity);
            contactDetailsEntities.add(contactDetailsEntity);
        }
        if (individualDetails.getHearingChannelPhone() != null) {
            final ContactDetailsEntity contactDetailsEntity = new ContactDetailsEntity();
            contactDetailsEntity.setContactDetails(individualDetails.getHearingChannelPhone());
            contactDetailsEntity.setContactType(PHONE_TYPE);
            contactDetailsEntity.setHearingParty(hearingPartyEntity);
            contactDetailsEntities.add(contactDetailsEntity);
        }
        return contactDetailsEntities;
    }
}
