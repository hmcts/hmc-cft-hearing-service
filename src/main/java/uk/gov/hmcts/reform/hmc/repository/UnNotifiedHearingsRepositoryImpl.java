package uk.gov.hmcts.reform.hmc.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import static uk.gov.hmcts.reform.hmc.constants.Constants.UN_NOTIFIED_HEARINGS_LIMIT;

@Slf4j
@Repository
public class UnNotifiedHearingsRepositoryImpl implements  UnNotifiedHearingsRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Long> getUnNotifiedHearingsWithOutStartDateTo(String hmctsServiceCode,
                                                              LocalDateTime hearingStartDateFrom,
                                                              List<String> hearingStatus, Pageable pageable) {
        StringBuffer hqlQuery = new StringBuffer("select hr.hearing.id FROM HearingResponseEntity hr "
            + "join HearingDayDetailsEntity hdd ON hr.hearingResponseId = hdd.hearingResponse.hearingResponseId "
            + "join CaseHearingRequestEntity csr ON hr.hearing.id = csr.hearing.id "
            + "join MaxHearingRequestVersionView mrv ON csr.hearing.id = mrv.hearingId "
            + "and csr.versionNumber = mrv.maxHearingRequestVersion "
            + "and hmcts_service_code = :hmctsServiceCode "
            + "where csr.hmctsServiceCode = :hmctsServiceCode "
            + "and hr.requestVersion = mrv.maxHearingRequestVersion "
            + "and hr.partiesNotifiedDateTime IS NULL "
            + "group BY hr.hearing.id "
            + "having min(hdd.startDateTime) >= :hearingStartDateFrom");
        /*if (null != hearingStatus && hearingStatus.contains("CANCELLED")) {
            hqlQuery.append(" OR hdd.startDateTime IS NULL");
        }*/
        Query query = em.createQuery(hqlQuery.toString());
        List hearings = query
            .setParameter("hmctsServiceCode", hmctsServiceCode)
            .setParameter("hearingStartDateFrom", hearingStartDateFrom)
            .setMaxResults(UN_NOTIFIED_HEARINGS_LIMIT)
            .getResultList();
        List<Long> hearingsLong = new ArrayList<>();
        Iterator it = hearings.iterator();
        while (it.hasNext()) {
            hearingsLong.add((Long) it.next());
        }
        return hearingsLong;
    }

    @Override
    public List<Long> getUnNotifiedHearingsWithStartDateTo(String hmctsServiceCode, LocalDateTime hearingStartDateFrom,
                                                           LocalDateTime hearingStartDateTo,
                                                           List<String> hearingStatus, Pageable pageable) {
        StringBuffer hqlQuery = new StringBuffer("select hr.hearing.id FROM HearingResponseEntity hr "
            + "join HearingDayDetailsEntity hdd ON hr.hearingResponseId = hdd.hearingResponse.hearingResponseId "
            + "join CaseHearingRequestEntity csr ON hr.hearing.id = csr.hearing.id "
            + "join MaxHearingRequestVersionView mrv ON csr.hearing.id = mrv.hearingId "
            + "and csr.versionNumber = mrv.maxHearingRequestVersion "
            + "and hmcts_service_code = :hmctsServiceCode "
            + "where csr.hmctsServiceCode = :hmctsServiceCode "
            + "and hr.requestVersion = mrv.maxHearingRequestVersion "
            + "and hr.partiesNotifiedDateTime IS NULL "
            + "group BY hr.hearing.id "
            + "having min(hdd.startDateTime) >= :hearingStartDateFrom "
            + "and max(hdd.endDateTime)<=:hearingStartDateTo");
        Query query = em.createQuery(hqlQuery.toString());
        List hearings = query
            .setParameter("hmctsServiceCode", hmctsServiceCode)
            .setParameter("hearingStartDateFrom", hearingStartDateFrom)
            .setParameter("hearingStartDateTo", hearingStartDateTo)
            .setMaxResults(UN_NOTIFIED_HEARINGS_LIMIT)
            .getResultList();
        List<Long> hearingsLong = new ArrayList<>();
        Iterator it = hearings.iterator();
        while (it.hasNext()) {
            hearingsLong.add((Long) it.next());
        }
        return hearingsLong;
    }
}
