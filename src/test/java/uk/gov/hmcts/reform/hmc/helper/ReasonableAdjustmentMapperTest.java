package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.ReasonableAdjustmentsEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReasonableAdjustmentMapperTest {

    @Test
    void modelToEntity() {
        ReasonableAdjustmentMapper mapper = new ReasonableAdjustmentMapper();
        HearingPartyEntity hearingPartyEntity = new HearingPartyEntity();
        List<String> reasonableAdjustments = getRoleAdjustments();
        List<ReasonableAdjustmentsEntity> entities = mapper.modelToEntity(reasonableAdjustments, hearingPartyEntity);
        assertEquals("First reason", entities.get(0).getReasonableAdjustmentCode());
        assertEquals("Second reason", entities.get(1).getReasonableAdjustmentCode());
    }

    private List<String> getRoleAdjustments() {
        List<String> reasonableAdjustments = new ArrayList<>();
        reasonableAdjustments.add("First reason");
        reasonableAdjustments.add("Second reason");
        return reasonableAdjustments;
    }
}
