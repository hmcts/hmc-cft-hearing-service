package uk.gov.hmcts.reform.hmc.utils;

import org.assertj.core.util.Lists;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.model.CaseCategory;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingLocation;
import uk.gov.hmcts.reform.hmc.model.HearingWindow;
import uk.gov.hmcts.reform.hmc.model.IndividualDetails;
import uk.gov.hmcts.reform.hmc.model.OrganisationDetails;
import uk.gov.hmcts.reform.hmc.model.PanelRequirements;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.RelatedParty;
import uk.gov.hmcts.reform.hmc.model.RequestDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_STATUS;

public class TestingUtil {

    private TestingUtil() {
    }

    public static RequestDetails requestDetails() {
        RequestDetails requestDetails = new RequestDetails();
        requestDetails.setRequestTimeStamp(LocalDateTime.now());
        return requestDetails;
    }

    public static HearingDetails hearingDetails() {

        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setAutoListFlag(true);
        hearingDetails.setHearingType("Some hearing type");
        HearingWindow hearingWindow = new HearingWindow();
        hearingWindow.setHearingWindowEndDateRange(LocalDate.parse("2017-03-01"));
        hearingWindow.setHearingWindowStartDateRange(LocalDate.parse("2017-03-01"));
        hearingDetails.setHearingWindow(hearingWindow);
        hearingDetails.setDuration(0);
        hearingDetails.setNonStandardHearingDurationReasons(Arrays.asList("First reason", "Second reason"));
        hearingDetails.setHearingPriorityType("Priority type");
        HearingLocation location1 = new HearingLocation();
        location1.setLocationId("COURT");
        location1.setLocationType("Location type");
        List<HearingLocation> hearingLocations = new ArrayList<>();
        hearingLocations.add(location1);
        hearingDetails.setHearingLocations(hearingLocations);
        return hearingDetails;
    }

    public static PanelRequirements panelRequirements() {
        PanelRequirements panelRequirements = new PanelRequirements();
        panelRequirements.setRoleType(Arrays.asList("RoleType1"));
        panelRequirements.setAuthorisationTypes(Arrays.asList("AuthorisationType1"));
        panelRequirements.setAuthorisationSubType(Arrays.asList("AuthorisationSubType2"));
        return panelRequirements;

    }

    public static CaseDetails caseDetails() {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setHmctsServiceCode("ABA1");
        caseDetails.setCaseRef("1111222233334444");
        caseDetails.setRequestTimeStamp(LocalDateTime.parse("2021-08-10T12:20:00"));
        caseDetails.setCaseDeepLink("https://www.google.com");
        caseDetails.setHmctsInternalCaseName("Internal case name");
        caseDetails.setPublicCaseName("Public case name");
        caseDetails.setCaseManagementLocationCode("CMLC123");
        caseDetails.setCaseRestrictedFlag(false);
        caseDetails.setCaseSlaStartDate(LocalDate.parse("2017-03-01"));
        CaseCategory category = new CaseCategory();
        category.setCategoryType("CASETYPE");
        category.setCategoryValue("PROBATE");
        List<CaseCategory> caseCategories = new ArrayList<>();
        caseCategories.add(category);
        caseDetails.setCaseCategories(caseCategories);
        return caseDetails;
    }

    public static List<PartyDetails> partyDetails() {
        PartyDetails partyDetails1 = new PartyDetails();
        partyDetails1.setPartyID("P1");
        partyDetails1.setPartyType("IND");
        partyDetails1.setPartyRole("DEF");

        PartyDetails partyDetails2 = new PartyDetails();
        partyDetails2.setPartyID("P2");
        partyDetails2.setPartyType("IND");
        partyDetails2.setPartyRole("DEF2");

        List<PartyDetails> partyDetails = Lists.newArrayList(partyDetails1, partyDetails2);
        return partyDetails;
    }

    public static IndividualDetails individualDetails() {
        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setTitle("Mr");
        individualDetails.setFirstName("firstName");
        individualDetails.setLastName("lastName");
        List<RelatedParty> relatedParties = new ArrayList<>();
        RelatedParty relatedParty1 = new RelatedParty();
        relatedParty1.setRelatedPartyID("P1");
        relatedParty1.setRelationshipType("R1");
        relatedParties.add(relatedParty1);
        RelatedParty relatedParty2 = new RelatedParty();
        relatedParty2.setRelatedPartyID("P2");
        relatedParty2.setRelationshipType("R2");
        relatedParties.add(relatedParty2);
        individualDetails.setRelatedParties(relatedParties);
        return individualDetails;
    }

    public static OrganisationDetails organisationDetails() {
        OrganisationDetails organisationDetails = new OrganisationDetails();
        organisationDetails.setName("name");
        organisationDetails.setOrganisationType("type");
        organisationDetails.setCftOrganisationID("cft");
        return organisationDetails;
    }

    public static HearingEntity hearingEntity() {
        HearingEntity hearingEntity = new HearingEntity();
        hearingEntity.setId(1L);
        hearingEntity.setStatus(HEARING_STATUS);
        CaseHearingRequestEntity caseHearingRequestEntity = caseHearingRequestEntity();
        hearingEntity.setCaseHearingRequest(caseHearingRequestEntity);
        return hearingEntity;
    }

    public static CaseHearingRequestEntity caseHearingRequestEntity() {
        CaseHearingRequestEntity entity = new CaseHearingRequestEntity();
        entity.setAutoListFlag(false);
        entity.setHearingType("Some hearing type");
        entity.setRequiredDurationInMinutes(10);
        entity.setHearingPriorityType("Priority type");
        entity.setHmctsServiceID("ABA1");
        entity.setCaseReference("1111222233334444");
        entity.setHearingRequestReceivedDateTime(LocalDateTime.parse("2000-08-10T12:20:00"));
        entity.setCaseUrlContextPath("https://www.google.com");
        entity.setHmctsInternalCaseName("Internal case name");
        entity.setOwningLocationId("CMLC123");
        entity.setCaseRestrictedFlag(true);
        entity.setCaseSlaStartDate(LocalDate.parse("2020-08-10"));
        entity.setVersionNumber(1);
        entity.setRequestTimeStamp(LocalDateTime.parse("2020-08-10T12:20:00"));
        return entity;

    }

    public static IndividualDetails individualContactDetails_HearingChannelEmail() {
        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setHearingChannelEmail("hearing.channel@email.com");
        return individualDetails;
    }

    public static IndividualDetails individualContactDetails_HearingChannelPhone() {
        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setHearingChannelPhone("01234567890");
        return individualDetails;
    }

    public static IndividualDetails individualContactDetails() {
        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setHearingChannelEmail("hearing.channel@email.com");
        individualDetails.setHearingChannelPhone("01234567890");
        return individualDetails;

    }
}
