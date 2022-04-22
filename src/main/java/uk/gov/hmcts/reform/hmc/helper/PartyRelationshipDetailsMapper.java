package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.PartyRelationshipDetailsEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.RelatedParty;
import uk.gov.hmcts.reform.hmc.repository.HearingPartyRepository;

import java.util.ArrayList;
import java.util.List;

@Component
public class PartyRelationshipDetailsMapper {

    private HearingPartyRepository hearingPartyRepository;

    @Autowired
    public PartyRelationshipDetailsMapper(HearingPartyRepository hearingPartyRepository) {
        this.hearingPartyRepository = hearingPartyRepository;
    }


    public List<PartyRelationshipDetailsEntity> modelToEntity(PartyDetails partyDetails) {

        List<RelatedParty> relatedParties = partyDetails.getIndividualDetails().getRelatedParties();
        List<PartyRelationshipDetailsEntity> partyRelationshipDetails = new ArrayList<>();

        if (relatedParties != null) {
            for (RelatedParty relatedParty: relatedParties) {

                String relatedPartyId = relatedParty.getRelatedPartyID();

                final HearingPartyEntity targetTechParty =
                        hearingPartyRepository.getTechPartyByReference(relatedPartyId);

                if (targetTechParty == null) {
                    throw new BadRequestException(
                            String.format("RelatedPartyId with value %s, does not exist in hearing_party db table",
                                    relatedPartyId));
                }

                final HearingPartyEntity sourceTechParty =
                        hearingPartyRepository.getTechPartyByReference(partyDetails.getPartyID());

                PartyRelationshipDetailsEntity partyRelationshipDetailsEntity = PartyRelationshipDetailsEntity
                        .builder()
                        .sourceTechParty(sourceTechParty)
                        .targetTechParty(targetTechParty)
                        .relationshipType(relatedParty.getRelationshipType())
                        .build();
                partyRelationshipDetails.add(partyRelationshipDetailsEntity);
            }
        }

        return partyRelationshipDetails;
    }
}
