package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.NonStandardDurationsEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NonStandardDurationsMapperTest {

    @Test
    void modelToEntityTest() {
        NonStandardDurationsMapper mapper = new NonStandardDurationsMapper();
        List<String> durations = getDurations();
        CaseHearingRequestEntity caseHearingRequestEntity = new CaseHearingRequestEntity();
        List<NonStandardDurationsEntity> entities = mapper.modelToEntity(durations, caseHearingRequestEntity);
        assertEquals("First reason", entities.get(0).getNonStandardHearingDurationReasonType());
        assertEquals("Second reason", entities.get(1).getNonStandardHearingDurationReasonType());
    }

    private List<String> getDurations() {
        List<String> durations = new ArrayList<>();
        durations.add("First reason");
        durations.add("Second reason");
        return durations;
    }
}
