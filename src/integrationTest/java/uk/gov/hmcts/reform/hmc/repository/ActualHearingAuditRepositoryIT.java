package uk.gov.hmcts.reform.hmc.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.data.ActualHearingAuditEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActualHearingAuditRepositoryIT extends BaseTest {

    private final ActualHearingAuditRepository actualHearingAuditRepository;

    private static final String INSERT_ACTUAL_HEARING_AUDIT_SCRIPT = "classpath:sql/insert-actual_hearing_audit.sql";
    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";

    @Autowired
    ActualHearingAuditRepositoryIT(ActualHearingAuditRepository actualHearingAuditRepository) {
        this.actualHearingAuditRepository = actualHearingAuditRepository;
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_ACTUAL_HEARING_AUDIT_SCRIPT})
    void findByHearingResponseId() {
        List<ActualHearingAuditEntity> auditEntityList = actualHearingAuditRepository.findByHearingResponseId(1L);
        assertEquals(1, auditEntityList.size());
        ActualHearingAuditEntity auditEntity = auditEntityList.getFirst();
        assertEquals(2000000000L, auditEntity.getHearingId());
        assertEquals(1L, auditEntity.getHearingResponseId());
        assertEquals("Some audit details for hearing 2000000000",
                     auditEntity.getActualHearingAuditRecord().get("auditDetails").asText());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_ACTUAL_HEARING_AUDIT_SCRIPT})
    void findByHearingId() {
        List<ActualHearingAuditEntity> auditEntityList = actualHearingAuditRepository.findByHearingId(2000000001L);
        assertEquals(1, auditEntityList.size());
        ActualHearingAuditEntity auditEntity = auditEntityList.getFirst();
        assertEquals(2000000001L, auditEntity.getHearingId());
        assertEquals(2L, auditEntity.getHearingResponseId());
        assertEquals("Some audit details for hearing 2000000001",
                     auditEntity.getActualHearingAuditRecord().get("auditDetails").asText());
    }

}
