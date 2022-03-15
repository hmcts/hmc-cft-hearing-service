package uk.gov.hmcts.reform.hmc.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LinkedGroupDetailsRepositoryIT extends BaseTest {
    private static final String INSERT_LINKED_HEARINGS_DATA_SCRIPT = "classpath:sql/insert-linked-hearings.sql";
    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";

    @Autowired
    LinkedGroupDetailsRepository linkedGroupDetailsRepository;

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
    void testFindByHearingGroupId() {
        Optional<LinkedGroupDetails> linkedGroupDetailsOptional = linkedGroupDetailsRepository.findById(7600000000L);
        assertTrue(linkedGroupDetailsOptional.isPresent());
        assertEquals(7600000000L, linkedGroupDetailsOptional.get().getLinkedGroupId());
        assertEquals("AWAITING_LISTING", linkedGroupDetailsOptional.get().getStatus());
    }
}
