package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.PartyRelationshipDetailsEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.RelatedParty;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PartyRelationshipDetailsMapper {

    public List<PartyRelationshipDetailsEntity> modelToEntity(PartyDetails partyDetails,
                                                              List<HearingPartyEntity> hearingPartyEntities) {

        List<RelatedParty> relatedParties = partyDetails.getIndividualDetails().getRelatedParties();
        List<PartyRelationshipDetailsEntity> partyRelationshipDetails = new ArrayList<>();

        if (!CollectionUtils.isEmpty(relatedParties)) {
            final HearingPartyEntity sourceTechParty =
                    getHearingPartyEntityByReference(partyDetails.getPartyID(), hearingPartyEntities);
            for (RelatedParty relatedParty: relatedParties) {

                String relatedPartyId = relatedParty.getRelatedPartyID();

                final HearingPartyEntity targetTechParty =
                        getHearingPartyEntityByReference(relatedPartyId, hearingPartyEntities);

                PartyRelationshipDetailsEntity partyRelationshipDetailsEntity = PartyRelationshipDetailsEntity
                        .builder()
                        .sourceTechParty(sourceTechParty)
                        .targetTechParty(targetTechParty)
                        .relationshipType(relatedParty.getRelationshipType())
                        .build();
                partyRelationshipDetails.add(partyRelationshipDetailsEntity);
            }
            sourceTechParty.setPartyRelationshipDetailsEntity(partyRelationshipDetails);
        }

        return partyRelationshipDetails;
    }

    private HearingPartyEntity getHearingPartyEntityByReference(String relatedPartyId,
                                                                List<HearingPartyEntity> hearingPartyEntities) {

        final List<HearingPartyEntity> matchingHearingPartyEntities = hearingPartyEntities.stream()
                .filter(hearingPartyEntity -> relatedPartyId.equals(hearingPartyEntity.getPartyReference()))
                .collect(Collectors.toList());

        if (matchingHearingPartyEntities.size() != 1) {
            throw new BadRequestException(
                            String.format("Cannot find unique PartyID with value %s", relatedPartyId));
        }

        return matchingHearingPartyEntities.get(0);
    }
}
