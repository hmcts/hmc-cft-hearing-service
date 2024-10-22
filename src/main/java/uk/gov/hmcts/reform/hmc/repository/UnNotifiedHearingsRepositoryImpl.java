package uk.gov.hmcts.reform.hmc.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import static uk.gov.hmcts.reform.hmc.constants.Constants.CANCELLED;
import static uk.gov.hmcts.reform.hmc.constants.Constants.UN_NOTIFIED_HEARINGS_LIMIT;

@Slf4j
@Repository
public class UnNotifiedHearingsRepositoryImpl implements UnNotifiedHearingsRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Long> getUnNotifiedHearingsWithOutStartDateTo(String hmctsServiceCode,
                                                              LocalDateTime hearingStartDateFrom,
                                                              List<String> hearingStatus) {
        StringBuilder hqlQuery = getQueryForUnNotifiedHearings(hearingStatus);
        if (hearingStatus != null && hearingStatus.contains(CANCELLED)) {
            hqlQuery.append(" OR hdd.startDateTime IS NULL");
        }

        Query query = em.createQuery(hqlQuery.toString())
                .setParameter("hmctsServiceCode", hmctsServiceCode)
                .setParameter("hearingStartDateFrom", hearingStartDateFrom)
                .setMaxResults(UN_NOTIFIED_HEARINGS_LIMIT);

        return getHearings(query.getResultList());
    }

    @Override
    public List<Long> getUnNotifiedHearingsWithStartDateTo(String hmctsServiceCode, LocalDateTime hearingStartDateFrom,
                                                           LocalDateTime hearingStartDateTo,
                                                           List<String> hearingStatus) {
        StringBuilder hqlQuery = getQueryForUnNotifiedHearings(hearingStatus);
        hqlQuery.append(" AND MAX(hdd.endDateTime) <= :hearingStartDateTo");
        if (hearingStatus != null && hearingStatus.contains(CANCELLED)) {
            hqlQuery.append(" OR hdd.startDateTime IS NULL");
        }

        Query query = em.createQuery(hqlQuery.toString())
                .setParameter("hmctsServiceCode", hmctsServiceCode)
                .setParameter("hearingStartDateFrom", hearingStartDateFrom)
                .setParameter("hearingStartDateTo", hearingStartDateTo)
                .setMaxResults(UN_NOTIFIED_HEARINGS_LIMIT);

        return getHearings(query.getResultList());
    }

    private StringBuilder getQueryForUnNotifiedHearings(List<String> hearingStatus) {
        StringBuilder hqlQuery = new StringBuilder("SELECT DISTINCT hr.hearing.id FROM HearingResponseEntity hr "
                + "JOIN CaseHearingRequestEntity csr ON hr.hearing.id = csr.hearing.id "
                + "JOIN MaxHearingRequestVersionView mrv ON csr.hearing.id = mrv.hearingId "
                + "JOIN HearingDayDetailsEntity hdd ON hr.hearingResponseId = hdd.hearingResponse.hearingResponseId "
                + "JOIN HearingEntity he ON hr.hearing.id = he.id "
                + "WHERE csr.hmctsServiceCode = :hmctsServiceCode "
                + "AND hr.requestVersion = mrv.maxHearingRequestVersion "
                + "AND hr.partiesNotifiedDateTime IS NULL ");

        if (hearingStatus != null && !hearingStatus.isEmpty()) {
            String statusConditions = hearingStatus.stream()
                    .map(status -> String.format("'%s'", status.toUpperCase()))
                    .collect(Collectors.joining(", ", "AND he.status IN (", ")"));
            hqlQuery.append(statusConditions);
        }

        hqlQuery.append(" AND (hdd.startDateTime >= :hearingStartDateFrom OR hdd.startDateTime IS NULL) ")
                .append("GROUP BY hr.hearing.id ")
                .append("HAVING MIN(hdd.startDateTime) >= :hearingStartDateFrom");

        return hqlQuery;
    }

    private List<Long> getHearings(List<?> hearings) {
        List<Long> hearingsLong = new ArrayList<>();
        for (Object hearing : hearings) {
            hearingsLong.add((Long) hearing);
        }
        return hearingsLong;
    }
}
