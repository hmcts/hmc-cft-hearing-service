package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.data.ContactDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.model.IndividualDetails;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.hmc.constants.Constants.EMAIL_TYPE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.PHONE_TYPE;

class ContactDetailMapperTest {

    @Test
    void modelToEntity_IndividualDetails_Email() {
        ContactDetailMapper contactDetailMapper = new ContactDetailMapper();
        IndividualDetails individualDetails = TestingUtil.individualContactDetails_HearingChannelEmail();
        HearingPartyEntity hearingPartyEntity = new HearingPartyEntity();
        List<ContactDetailsEntity> entities = contactDetailMapper.modelToEntity(individualDetails, hearingPartyEntity);
        assertEquals("hearing.channel@email.com", entities.get(0).getContactDetails());
        assertEquals(EMAIL_TYPE, entities.get(0).getContactType());

    }

    @Test
    void modelToEntity_IndividualDetails_Phone() {
        ContactDetailMapper contactDetailMapper = new ContactDetailMapper();
        IndividualDetails individualDetails = TestingUtil.individualContactDetails_HearingChannelPhone();
        HearingPartyEntity hearingPartyEntity = new HearingPartyEntity();
        List<ContactDetailsEntity> entities = contactDetailMapper.modelToEntity(individualDetails, hearingPartyEntity);
        assertEquals("01234567890", entities.get(0).getContactDetails());
        assertEquals(PHONE_TYPE, entities.get(0).getContactType());
    }

    @Test
    void modelToEntity_IndividualContactDetails() {
        ContactDetailMapper contactDetailMapper = new ContactDetailMapper();
        IndividualDetails individualDetails = TestingUtil.individualContactDetails();
        HearingPartyEntity hearingPartyEntity = new HearingPartyEntity();
        List<ContactDetailsEntity> entities = contactDetailMapper.modelToEntity(individualDetails, hearingPartyEntity);
        assertEquals("hearing.channel@email.com", entities.get(0).getContactDetails());
        assertEquals(EMAIL_TYPE, entities.get(0).getContactType());
        assertEquals("01234567890", entities.get(1).getContactDetails());
        assertEquals(PHONE_TYPE, entities.get(1).getContactType());
    }

}
