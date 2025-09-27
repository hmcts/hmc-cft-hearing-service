/*
Scenario 5 - verify audit table data - before migration
Make a note of the count of records from the below 4 scenarios and sum up all the counts.
The final count should match the count of records from the audit table after migration.
*/

-- count of records from the audit table before migration
select count(*) FROM hearing_status_audit;

-- Scenario 1 - non Tribunals count
SELECT count(*)
        FROM hearing h
		    left join case_hearing_request chr on h.hearing_id = chr.hearing_id
        WHERE chr.hmcts_service_code in ('AAA6','AAA7' ,'ABA5');

-- Scenario 2 - Tribunals - Judge Roles = 1
select count(*) from (
select h.hearing_id,chr.hmcts_service_code,h.status,h.updated_date_time,chr.hearing_request_version
        FROM hearing h
        left join case_hearing_request chr on h.hearing_id = chr.hearing_id
        left join hearing_response hr on h.hearing_id = hr.hearing_id
        join hearing_day_details hdd on hdd.hearing_response_id = hr.hearing_response_id
        join hearing_day_panel hdp on hdp.hearing_day_id = hdd.hearing_day_id
        WHERE chr.hmcts_service_code not in ('AAA6','AAA7' ,'ABA5')
        group by hdp.hearing_day_id,h.hearing_id,chr.hmcts_service_code,h.status,h.updated_date_time,chr.hearing_request_version
        HAVING COUNT(hdp.hearing_day_id) = 1) as totalRecords;

-- Scenario 3 - Tribunals - Judge Roles > 1 count
select count(*) from (
select h.hearing_id,chr.hmcts_service_code,h.status,h.updated_date_time,chr.hearing_request_version
        FROM hearing h
        left join case_hearing_request chr on h.hearing_id = chr.hearing_id
        left join hearing_response hr on h.hearing_id = hr.hearing_id
        join hearing_day_details hdd on hdd.hearing_response_id = hr.hearing_response_id
        join hearing_day_panel hdp on hdp.hearing_day_id = hdd.hearing_day_id
        WHERE chr.hmcts_service_code not in ('AAA6','AAA7' ,'ABA5')
        group by hdp.hearing_day_id,h.hearing_id,chr.hmcts_service_code,h.status,h.updated_date_time,chr.hearing_request_version
        HAVING COUNT(hdp.hearing_day_id) > 1) as totalRecords;

-- Scenario 4 - Tribunals - Judge Roles not specified count
select count(*)
        FROM hearing h
        left join case_hearing_request chr on h.hearing_id = chr.hearing_id
        left join hearing_response hr on h.hearing_id = hr.hearing_id
        WHERE chr.hmcts_service_code not in ('AAA6','AAA7' ,'ABA5')
        and hr.hearing_response_id in(
        select hdd.hearing_response_id from hearing_day_details hdd where hdd.hearing_day_id not in(
        select hdp.hearing_day_id from hearing_day_panel hdp));


-- count of records from the audit table after migration
select count(*) FROM hearing_status_audit;
