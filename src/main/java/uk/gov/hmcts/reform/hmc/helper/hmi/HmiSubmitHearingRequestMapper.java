package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.hmi.CancellationReason;
import uk.gov.hmcts.reform.hmc.model.hmi.HmiDeleteHearingRequest;
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

    /**
     * map hearing Request to HMI Submit Hearing Request.
     * @param hearingId haring id
     * @param hearingRequest hearing request
     * @return hmiSubmitHearingRequest HMI Submit Hearing request object
     */
    public HmiSubmitHearingRequest mapRequest(Long hearingId, HearingRequest hearingRequest) {
        EntitiesMapperObject entities = entitiesMapper.getEntities(hearingRequest.getPartyDetails());

        HmiHearingRequest hmiHearingRequest = HmiHearingRequest.builder()
            .caseDetails(hmiCaseDetailsMapper.getCaseDetails(hearingRequest.getCaseDetails(), hearingId))
            .entities(entities.getEntities())
            .listing(listingMapper.getListing(hearingRequest.getHearingDetails(), entities
                .getPreferredHearingChannels()))
            .build();

        return HmiSubmitHearingRequest.builder()
            .hearingRequest(hmiHearingRequest)
            .build();
    }

    /**
     * map Delete hearing Request to HMI Submit Delete Hearing Request.
     * @param hearingRequest delete hearing request
     * @return hmiDeleteHearingRequest HMI Delete Hearing request object
     */
    public HmiDeleteHearingRequest mapRequest(DeleteHearingRequest hearingRequest) {

        return HmiDeleteHearingRequest.builder()
                .cancellationReason(new CancellationReason(hearingRequest.getCancellationReasonCode()))
                .build();
    }

}
