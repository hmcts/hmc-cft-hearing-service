package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.model.CreateHearingRequest;
import uk.gov.hmcts.reform.hmc.model.UpdateHearingRequest;
import uk.gov.hmcts.reform.hmc.model.hmi.HmiHearingRequest;
import uk.gov.hmcts.reform.hmc.model.hmi.HmiSubmitHearingRequest;

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

    public HmiSubmitHearingRequest mapRequest(Long hearingId, CreateHearingRequest hearingRequest) {
        EntitiesMapperObject entities = entitiesMapper.getEntities(hearingRequest.getPartyDetails());

        HmiHearingRequest hmiHearingRequest = HmiHearingRequest.builder()
            .caseDetails(hmiCaseDetailsMapper.getCaseDetails(
                    hearingRequest.getCaseDetails(), hearingId))
            .entities(entities.getEntities())
            .listing(listingMapper.getListing(hearingRequest.getHearingDetails(), entities
                .getPreferredHearingChannels()))
            .build();

        return HmiSubmitHearingRequest.builder()
            .hearingRequest(hmiHearingRequest)
            .build();
    }

    public HmiSubmitHearingRequest mapRequest(Long hearingId, UpdateHearingRequest hearingRequest) {
        EntitiesMapperObject entities = entitiesMapper.getEntities(hearingRequest.getPartyDetails());

        HmiHearingRequest hmiHearingRequest = HmiHearingRequest.builder()
                .caseDetails(hmiCaseDetailsMapper.getCaseDetails(
                        hearingRequest.getCaseDetails(),
                        hearingRequest.getRequestDetails().getVersionNumber(),
                        hearingId))
                .entities(entities.getEntities())
                .listing(listingMapper.getListing(hearingRequest.getHearingDetails(), entities
                        .getPreferredHearingChannels()))
                .build();

        return HmiSubmitHearingRequest.builder()
                .hearingRequest(hmiHearingRequest)
                .build();
    }

}
