package uk.gov.hmcts.reform.hmc.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.data.ActualHearingAuditEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ActualHearingAuditRepositoryIT extends BaseTest {

    @Autowired
    ActualHearingAuditRepository actualHearingAuditRepository;

    private static final String INSERT_ACTUAL_HEARING_AUDIT_SCRIPT = "classpath:sql/insert-actual_hearing_audit.sql";
    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_ACTUAL_HEARING_AUDIT_SCRIPT})
    void findByHearingResponseId() {
        List<ActualHearingAuditEntity> auditEntityList = actualHearingAuditRepository.findByHearingResponseId(1L);
        assertEquals(1, auditEntityList.size());
        assertEquals(2000000000L, auditEntityList.get(0).getHearingId());
        assertEquals(1L, auditEntityList.get(0).getHearingResponseId());
        assertEquals("Some audit details for hearing 2000000000",
                     auditEntityList.get(0).getActualHearingAuditRecord().get("auditDetails").asText());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_ACTUAL_HEARING_AUDIT_SCRIPT})
    void findByHearingId() {
        List<ActualHearingAuditEntity> auditEntityList = actualHearingAuditRepository.findByHearingId(2000000001L);
        assertEquals(1, auditEntityList.size());
        assertEquals(2000000001L, auditEntityList.get(0).getHearingId());
        assertEquals(2L, auditEntityList.get(0).getHearingResponseId());
        assertEquals("Some audit details for hearing 2000000001",
                     auditEntityList.get(0).getActualHearingAuditRecord().get("auditDetails").asText());
    }

}
