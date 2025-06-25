package uk.gov.hmcts.reform.hmc.data;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.REQUIRES_NEW)
@Repository
public interface TestLinkedGroupDetailsRepository extends CrudRepository<LinkedGroupDetails, Long> {

    @Query(value = "SELECT nextval('public.linked_group_details_id_seq')", nativeQuery = true)
    Integer getNextVal();

    @Modifying
    @Query(value = "ALTER SEQUENCE public.linked_group_details_id_seq RESTART WITH 1000", nativeQuery = true)
    void resetCurrentIndex();

}
