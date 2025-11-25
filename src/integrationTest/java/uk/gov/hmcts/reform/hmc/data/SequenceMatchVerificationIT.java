package uk.gov.hmcts.reform.hmc.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.domain.model.enums.LinkType;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

public class SequenceMatchVerificationIT extends BaseTest {

    private static final String REQUEST_ID = "testSequenceMatchVerification#";
    private static final String CLEANUP_SQL = "classpath:sql/linked_hearing_group-sequence-cleanup.sql";

    @Autowired
    private TestLinkedGroupDetailsRepository repository;

    @Test
    @Sql(scripts = {CLEANUP_SQL}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {CLEANUP_SQL}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testSequenceMatchVerification() {
        repository.save(createLinkedGroupDetails(1));
        repository.save(createLinkedGroupDetails(2));
        repository.save(createLinkedGroupDetails(3));
        assertEquals(4, repository.getNextVal().intValue());
        repository.resetCurrentIndex();
        assertEquals(1000, repository.getNextVal().intValue());
        repository.save(createLinkedGroupDetails(1));
        repository.save(createLinkedGroupDetails(2));
        repository.save(createLinkedGroupDetails(3));
        assertEquals(1004, repository.getNextVal().intValue());
    }

    private LinkedGroupDetails createLinkedGroupDetails(int id) {
        LinkedGroupDetails linkedGroupDetails = new LinkedGroupDetails();
        linkedGroupDetails.setRequestId(REQUEST_ID + id);
        linkedGroupDetails.setRequestName(REQUEST_ID + id);
        linkedGroupDetails.setRequestDateTime(LocalDateTime.now());
        linkedGroupDetails.setLinkType(LinkType.ORDERED);
        linkedGroupDetails.setReasonForLink(REQUEST_ID + id);
        linkedGroupDetails.setStatus(REQUEST_ID + id);
        linkedGroupDetails.setLinkedComments(REQUEST_ID + id);
        linkedGroupDetails.setLinkedGroupLatestVersion((long) id);
        return linkedGroupDetails;
    }
    
}
