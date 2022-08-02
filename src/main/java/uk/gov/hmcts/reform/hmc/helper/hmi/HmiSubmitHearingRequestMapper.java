package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.hmi.HmiCaseDetails;
import uk.gov.hmcts.reform.hmc.model.hmi.HmiHearingRequest;
import uk.gov.hmcts.reform.hmc.model.hmi.HmiSubmitHearingRequest;
import uk.gov.hmcts.reform.hmc.model.hmi.Listing;

@Component
public class HmiSubmitHearingRequestMapper {

    private final HmiCaseDetailsMapper hmiCaseDetailsMapper;
    private final EntitiesMapper entitiesMapper;
    private final ListingMapper listingMapper;

    @Autowired
    public HmiSubmitHearingRequestMapper(HmiCaseDetailsMapper hmiCaseDetailsMapper, EntitiesMapper entitiesMapper,
                                         ListingMapper listingMapper) {
        this.hmiCaseDetailsMapper = hmiCaseDetailsMapper;
        this.entitiesMapper = entitiesMapper;
        this.listingMapper = listingMapper;
    }

    public HmiSubmitHearingRequest mapRequest(HearingRequest hearingRequest,
                                              HmiCaseDetails caseDetails,
                                              Listing listing) {
        EntitiesMapperObject entities = entitiesMapper.getEntities(hearingRequest.getPartyDetails());
        HmiHearingRequest hmiHearingRequest = HmiHearingRequest.builder()
            .caseDetails(caseDetails)
            .entities(entities.getEntities())
            .listing(listing)
            .build();

        return HmiSubmitHearingRequest.builder()
            .hearingRequest(hmiHearingRequest)
            .build();
    }

}
