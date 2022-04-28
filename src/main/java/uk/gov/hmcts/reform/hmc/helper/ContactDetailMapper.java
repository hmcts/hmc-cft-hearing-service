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
            contactDetailsEntities.addAll(
                createContactDetailEntities(individualDetails.getHearingChannelEmail(), hearingPartyEntity, EMAIL_TYPE)
            );
        }
        if (individualDetails.getHearingChannelPhone() != null) {
            contactDetailsEntities.addAll(
                createContactDetailEntities(individualDetails.getHearingChannelPhone(), hearingPartyEntity, PHONE_TYPE)
            );
        }
        return contactDetailsEntities;
    }

    private List<ContactDetailsEntity> createContactDetailEntities(List<String> contactDetails,
                                                                   HearingPartyEntity hearingPartyEntity,
                                                                   String contactType) {
        List<ContactDetailsEntity> contactDetailsEntities = new ArrayList<>();
        for (String contactDetail : contactDetails) {
            final ContactDetailsEntity contactDetailsEntity = new ContactDetailsEntity();
            contactDetailsEntity.setContactDetails(contactDetail);
            contactDetailsEntity.setContactType(contactType);
            contactDetailsEntity.setHearingParty(hearingPartyEntity);
            contactDetailsEntities.add(contactDetailsEntity);
        }
        return contactDetailsEntities;
    }
}
