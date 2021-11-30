package uk.gov.hmcts.reform.hmc.utils;

import org.assertj.core.util.Lists;
import uk.gov.hmcts.reform.hmc.model.CaseCategory;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingLocation;
import uk.gov.hmcts.reform.hmc.model.HearingWindow;
import uk.gov.hmcts.reform.hmc.model.HearingWindowDateRange;
import uk.gov.hmcts.reform.hmc.model.HearingWindowFirstDate;
import uk.gov.hmcts.reform.hmc.model.IndividualDetails;
import uk.gov.hmcts.reform.hmc.model.OrganisationDetails;
import uk.gov.hmcts.reform.hmc.model.PanelRequirements;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.RequestDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        HearingWindowDateRange hearingWindowDateRange = new HearingWindowDateRange();
        hearingWindow.setHearingWindowDateRange(hearingWindowDateRange);
        hearingWindow.getHearingWindowDateRange()
            .setHearingWindowEndDateRange(LocalDate.parse("2017-03-01"));
        hearingWindow.getHearingWindowDateRange()
            .setHearingWindowStartDateRange(LocalDate.parse("2017-03-01"));
        HearingWindowFirstDate hearingWindowFirstDate = new HearingWindowFirstDate();
        hearingWindow.setHearingWindowFirstDate(hearingWindowFirstDate);
        hearingWindow.getHearingWindowFirstDate()
            .setFirstDateTimeMustBe(LocalDateTime.parse("2017-04-01T08:00:01"));
        hearingDetails.setHearingWindow(hearingWindow);
        hearingDetails.setDuration(0);
        hearingDetails.setNonStandardHearingDurationReasons(
            Arrays.asList("First reason", "Second reason"));
        hearingDetails.setHearingPriorityType("Priority type");
        HearingLocation location1 = new HearingLocation();
        location1.setLocationId("court");
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
        caseDetails.setCaseRef("9372710950276233");
        caseDetails.setRequestTimeStamp(LocalDateTime.parse("2021-08-10T12:20:00"));
        caseDetails.setCaseDeepLink("https://www.google.com");
        caseDetails.setHmctsInternalCaseName("Internal case name");
        caseDetails.setPublicCaseName("Public case name");
        caseDetails.setCaseManagementLocationCode("CMLC123");
        caseDetails.setCaseRestrictedFlag(false);
        caseDetails.setCaseSlaStartDate(LocalDate.parse("2017-03-01"));
        CaseCategory category = new CaseCategory();
        category.setCategoryType("caseType");
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

        List<PartyDetails> partyDetails = Lists.newArrayList(partyDetails1,partyDetails2);
        return partyDetails;
    }

    public static IndividualDetails individualDetails() {
        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setTitle("Mr");
        individualDetails.setFirstName("firstName");
        individualDetails.setLastName("lastName");
        return individualDetails;
    }

    public static OrganisationDetails organisationDetails() {
        OrganisationDetails organisationDetails = new OrganisationDetails();
        organisationDetails.setName("name");
        organisationDetails.setOrganisationType("type");
        organisationDetails.setCftOrganisationID("cft");
        return organisationDetails;
    }

}
