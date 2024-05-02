package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingStatusAuditEntity;
import uk.gov.hmcts.reform.hmc.model.HearingStatusAudit;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.hmc.constants.Constants.DELETE_LINKED_HEARING_REQUEST;
import static uk.gov.hmcts.reform.hmc.constants.Constants.SUCCESS_STATUS;

public class LinkedHearingStatusAuditMapperTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2021-08-10T12:20:00Z"), ZoneOffset.UTC);

    @Test
    void modelToEntity() {
        LinkedHearingStatusAuditMapper mapper = new LinkedHearingStatusAuditMapper(CLOCK);
        HearingStatusAudit hearingStatusAudit = TestingUtil.hearingStatusAudit(DELETE_LINKED_HEARING_REQUEST);
        LinkedHearingStatusAuditEntity entity = mapper.modelToEntity(hearingStatusAudit);
        assertEquals(hearingStatusAudit.getHearingServiceId(), entity.getHmctsServiceId());
        assertEquals(SUCCESS_STATUS, entity.getHttpStatus());
        assertEquals(DELETE_LINKED_HEARING_REQUEST, entity.getHearingEvent());
    }
}
