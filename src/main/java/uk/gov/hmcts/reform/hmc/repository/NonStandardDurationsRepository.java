package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.CancellationReasonsEntity;
import uk.gov.hmcts.reform.hmc.data.NonStandardDurationsEntity;

@Transactional(propagation = Propagation.REQUIRES_NEW)
@Repository
public interface NonStandardDurationsRepository extends CrudRepository<NonStandardDurationsEntity, Long> {

}
