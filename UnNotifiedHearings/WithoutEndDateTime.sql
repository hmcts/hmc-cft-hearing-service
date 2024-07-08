update hearing_response hr
set parties_notified_datetime =:parties_notified_datetime
from case_hearing_request csr
where hr.hearing_id = csr.hearing_id
and hr.hearing_id in (
SELECT distinct hr.hearing_id
    FROM hearing_response hr
    JOIN case_hearing_request csr ON hr.hearing_id = csr.hearing_id
    JOIN max_hearing_request_version mrv ON csr.hearing_id = mrv.hearing_id
    JOIN hearing_day_details hdd ON hr.hearing_response_id = hdd.hearing_response_id
    JOIN hearing he ON hr.hearing_id = he.hearing_id
    WHERE csr.hmcts_service_code = :hmcts_service_code
      AND (hr.request_version = mrv.max_hearing_request_version OR (he.status = 'CANCELLED' AND he.hearing_id = hr.hearing_id))
      AND hr.parties_notified_datetime IS NULL
      AND (hdd.start_date_time >= :hearing_start_date_from
       OR hdd.start_date_time IS NULL)
    GROUP BY hr.hearing_id, he.status, start_date_time
    HAVING
        MIN(hdd.start_date_time) >= :hearing_start_date_from
        OR hdd.start_date_time IS null
)
and csr.hmcts_service_code = :hmcts_service_code
