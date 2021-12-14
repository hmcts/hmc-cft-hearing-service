package uk.gov.hmcts.reform.hmc.helper.hmi;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.hmc.model.hmi.Entity;

import java.util.List;

@Builder
@Data
public class EntitiesMapperObject {

    private List<Entity> entities;

    private List<String> preferredHearingChannels;
}
