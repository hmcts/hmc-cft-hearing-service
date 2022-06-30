package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.ChangeReasonsEntity;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChangeReasonsMapperTest {

    @Test
    void modelToEntityTest() {
        ChangeReasonsMapper mapper = new ChangeReasonsMapper();
        List<String> changeReasons = List.of("first reason", "second reason", "third reason");
        List<ChangeReasonsEntity> entities = mapper.modelToEntity(changeReasons, new CaseHearingRequestEntity());

        assertAll(
                () -> assertEquals(changeReasons.size(), entities.size()),
                () -> assertTrue(
                        entities.stream()
                                .map(ChangeReasonsEntity::getChangeReasonType)
                                .collect(Collectors.toList())
                                .containsAll(changeReasons)
                )
        );
    }

}
