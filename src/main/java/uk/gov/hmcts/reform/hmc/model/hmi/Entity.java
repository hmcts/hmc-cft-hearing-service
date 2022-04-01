package uk.gov.hmcts.reform.hmc.model.hmi;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Entity {

    private String entityId;

    private String entityTypeCode;

    private String entityRoleCode;

    private EntitySubType entitySubType;

    private List<EntityUnavailableDay> entityUnavailableDays;

    private List<EntityUnavailableDate> entityUnavailableDates;

    private List<EntityCommunication> entityCommunications;

    private String entityHearingChannel;

    private List<RelatedEntity> entityRelatedEntities;

    private List<String> entityOtherConsiderations;
}
