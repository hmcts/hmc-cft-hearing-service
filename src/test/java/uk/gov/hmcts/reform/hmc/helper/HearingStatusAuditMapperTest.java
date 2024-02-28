package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.data.HearingStatusAuditEntity;
import uk.gov.hmcts.reform.hmc.model.HearingStatusAudit;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.hmc.constants.Constants.SUCCESS_STATUS;

class HearingStatusAuditMapperTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2021-08-10T12:20:00Z"), ZoneOffset.UTC);

    @Test
    void modelToEntity() {
        HearingStatusAuditMapper mapper = new HearingStatusAuditMapper(CLOCK);
        HearingStatusAudit hearingStatusAudit = TestingUtil.hearingStatusAudit();
        HearingStatusAuditEntity entity = mapper.modelToEntity(hearingStatusAudit);
        assertEquals("ABA1", entity.getHmctsServiceId());
        assertEquals(SUCCESS_STATUS, entity.getHttpStatus());
        assertEquals("create-hearing- request", entity.getHearingEvent());
    }
}
