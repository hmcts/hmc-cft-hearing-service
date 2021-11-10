package uk.gov.hmcts.reform.hmc.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.helper.CaseHearingRequestMapper;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class HearingRepositoryImpl implements  HearingRepository  {

    private final CaseHearingRequestMapper caseHearingRequestMapper;

    @Autowired
    public HearingRepositoryImpl(CaseHearingRequestMapper caseHearingRequestMapper) {
        this.caseHearingRequestMapper = caseHearingRequestMapper;
    }

    @Override
    public HearingEntity findHearing(Long id) {
        return null;
    }

    @Override
    public HearingResponse saveHearing(HearingRequest hearingRequest) {

        CaseHearingRequestEntity caseHearingRequestEntity = caseHearingRequestMapper
            .modelToEntity(hearingRequest.getRequestDetails());

        caseHearingRequestMapper.modelToEntity(hearingRequest.getHearingDetails(),
                                                                          caseHearingRequestEntity);
        HearingResponse response = new HearingResponse();
        response.setHearingRequestId(2000000L);
        response.setStatus("Requested");
        response.setTimeStamp(LocalDateTime.now());
        return response;
    }

    @Override
    public <S extends HearingEntity> S save(S entity) {
        return null;
    }

    @Override
    public <S extends HearingEntity> Iterable<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<HearingEntity> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(Long id) {
        return false;
    }

    @Override
    public Iterable<HearingEntity> findAll() {
        return null;
    }

    @Override
    public Iterable<HearingEntity> findAllById(Iterable<Long> longs) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(Long id) {

    }

    @Override
    public void delete(HearingEntity entity) {

    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {

    }

    @Override
    public void deleteAll(Iterable<? extends HearingEntity> entities) {

    }

    @Override
    public void deleteAll() {

    }
}
