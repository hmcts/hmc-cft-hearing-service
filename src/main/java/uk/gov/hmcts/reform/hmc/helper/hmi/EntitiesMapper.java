package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.hmi.Entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Component
public class EntitiesMapper {

    private final UnavailableDaysMapper unavailableDaysMapper;
    private final UnavailableDatesMapper unavailableDatesMapper;
    private final CommunicationsMapper communicationsMapper;
    private final RelatedEntitiesMapper relatedEntitiesMapper;
    private final EntitySubTypeMapper entitySubTypeMapper;

    @Autowired
    public EntitiesMapper(UnavailableDaysMapper unavailableDaysMapper, UnavailableDatesMapper unavailableDatesMapper,
                          CommunicationsMapper communicationsMapper, RelatedEntitiesMapper relatedEntitiesMapper,
                          EntitySubTypeMapper entitySubTypeMapper) {
        this.unavailableDaysMapper = unavailableDaysMapper;
        this.unavailableDatesMapper = unavailableDatesMapper;
        this.communicationsMapper = communicationsMapper;
        this.relatedEntitiesMapper = relatedEntitiesMapper;
        this.entitySubTypeMapper = entitySubTypeMapper;
    }

    public EntitiesMapperObject getEntities(List<PartyDetails> partyDetails) {
        List<Entity> entities = new ArrayList<>();
        HashSet<String> preferredHearingChannels = new HashSet<>();
        if (partyDetails != null) {
            for (PartyDetails party : partyDetails) {
                if (party.getIndividualDetails() != null) {
                    Entity entity = Entity.builder()
                        .entityId(party.getPartyID())
                        .entityTypeCode(party.getPartyType())
                        .entityRoleCode(party.getPartyRole())
                        .entitySubType(entitySubTypeMapper.getPersonEntitySubType(party.getIndividualDetails()))
                        .entityHearingChannel(party.getIndividualDetails().getPreferredHearingChannel())
                        .entityCommunications(communicationsMapper.getCommunications(party))
                        .entityOtherConsiderations(party.getIndividualDetails().getReasonableAdjustments())
                        .entityUnavailableDays(unavailableDaysMapper.getUnavailableDays(party.getUnavailabilityDow()))
                        .entityUnavailableDates(unavailableDatesMapper
                                                    .getUnavailableDates(party.getUnavailabilityRanges()))
                        .entityRelatedEntities(relatedEntitiesMapper.getRelatedEntities(party.getIndividualDetails()
                                                                                            .getRelatedParties()))
                        .build();

                    entities.add(entity);
                    if (party.getIndividualDetails().getPreferredHearingChannel() != null) {
                        preferredHearingChannels.add(party.getIndividualDetails().getPreferredHearingChannel());
                    }
                } else if (party.getOrganisationDetails() != null) {

                    Entity entity = Entity.builder()
                        .entityId(party.getPartyID())
                        .entityTypeCode(party.getPartyType())
                        .entityRoleCode(party.getPartyRole())
                        .entitySubType(entitySubTypeMapper.getOrgEntitySubType(party.getOrganisationDetails()))
                        .build();
                    entities.add(entity);
                }
            }
        }
        List<String> uniquePreferredHearingChannels = new ArrayList<>(preferredHearingChannels);
        return EntitiesMapperObject.builder()
            .entities(entities)
            .preferredHearingChannels(uniquePreferredHearingChannels)
            .build();
    }
}
