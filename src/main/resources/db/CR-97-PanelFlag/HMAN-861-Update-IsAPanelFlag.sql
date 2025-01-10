
/* Scenario 1 - non Tribunals */
DO $$
DECLARE
    rec RECORD;
BEGIN
    FOR rec IN
        SELECT h.hearing_id,chr.hmcts_service_code,h.status,h.updated_date_time,chr.hearing_request_version
        FROM hearing h
		    left join case_hearing_request chr on h.hearing_id = chr.hearing_id
        WHERE chr.hmcts_service_code in ('AAA6','AAA7' ,'ABA5')
    LOOP
        UPDATE case_hearing_request
        SET is_a_panel_flag = false
        WHERE hearing_id = rec.hearing_id;

		INSERT INTO hearing_status_audit(hmcts_service_id,hearing_id,status,status_update_date_time,hearing_event,request_version,response_date_time,other_info)
		VALUES (rec.hmcts_service_code,rec.hearing_id,rec.status,rec.updated_date_time,'CR-97-panel_flag migration',rec.hearing_request_version,
				    CURRENT_TIMESTAMP,TO_JSON('{"HMAN Ticket": "HMAN-861", "CR": "CR-97-DB Migration"}'::JSONB));
		END LOOP;
END $$;

/* Scenario 2 - Tribunals - Judge Roles = 1 */
DO $$
DECLARE
    rec RECORD;
BEGIN
    FOR rec IN
        select h.hearing_id,chr.hmcts_service_code,h.status,h.updated_date_time,chr.hearing_request_version
        FROM hearing h
        left join case_hearing_request chr on h.hearing_id = chr.hearing_id
        left join hearing_response hr on h.hearing_id = hr.hearing_id
        join hearing_day_details hdd on hdd.hearing_response_id = hr.hearing_response_id
        join hearing_day_panel hdp on hdp.hearing_day_id = hdd.hearing_day_id
        WHERE chr.hmcts_service_code not in ('AAA6','AAA7' ,'ABA5')
        group by hdp.hearing_day_id,h.hearing_id,chr.hmcts_service_code,h.status,h.updated_date_time,chr.hearing_request_version
        HAVING COUNT(hdp.hearing_day_id) = 1
    LOOP
        UPDATE case_hearing_request
        SET is_a_panel_flag = false
        WHERE hearing_id = rec.hearing_id;

		INSERT INTO hearing_status_audit(hmcts_service_id,hearing_id,status,status_update_date_time,hearing_event,request_version,response_date_time,other_info)
		VALUES (rec.hmcts_service_code,rec.hearing_id,rec.status,rec.updated_date_time,'CR-97-panel_flag migration',rec.hearing_request_version,
				    CURRENT_TIMESTAMP,TO_JSON('{"HMAN Ticket": "HMAN-861", "CR": "CR-97-DB Migration"}'::JSONB));
		END LOOP;
END $$;

/* Scenario 3 - Tribunals - Judge Roles > 1 */
DO $$
DECLARE
    rec RECORD;
BEGIN
    FOR rec IN
        select h.hearing_id,chr.hmcts_service_code,h.status,h.updated_date_time,chr.hearing_request_version
        FROM hearing h
        left join case_hearing_request chr on h.hearing_id = chr.hearing_id
        left join hearing_response hr on h.hearing_id = hr.hearing_id
        join hearing_day_details hdd on hdd.hearing_response_id = hr.hearing_response_id
        join hearing_day_panel hdp on hdp.hearing_day_id = hdd.hearing_day_id
        WHERE chr.hmcts_service_code not in ('AAA6','AAA7' ,'ABA5')
        group by hdp.hearing_day_id,h.hearing_id,chr.hmcts_service_code,h.status,h.updated_date_time,chr.hearing_request_version
        HAVING COUNT(hdp.hearing_day_id) > 1
    LOOP
        UPDATE case_hearing_request
        SET is_a_panel_flag = true
        WHERE hearing_id = rec.hearing_id;

		INSERT INTO hearing_status_audit(hmcts_service_id,hearing_id,status,status_update_date_time,hearing_event,request_version,response_date_time,other_info)
		VALUES (rec.hmcts_service_code,rec.hearing_id,rec.status,rec.updated_date_time,'CR-97-panel_flag migration',rec.hearing_request_version,
				    CURRENT_TIMESTAMP,TO_JSON('{"HMAN Ticket": "HMAN-861", "CR": "CR-97-DB Migration"}'::JSONB));
		END LOOP;
END $$;

/* Scenario 4 - Tribunals - Judge Roles not specified */
DO $$
DECLARE
    rec RECORD;
BEGIN
    FOR rec IN
        select h.hearing_id,chr.hmcts_service_code,h.status,h.updated_date_time,chr.hearing_request_version
        FROM hearing h
        left join case_hearing_request chr on h.hearing_id = chr.hearing_id
        left join hearing_response hr on h.hearing_id = hr.hearing_id
        WHERE chr.hmcts_service_code not in ('AAA6','AAA7' ,'ABA5')
        and hr.hearing_response_id in(
        select hdd.hearing_response_id from hearing_day_details hdd where hdd.hearing_day_id not in(
        select hdp.hearing_day_id from hearing_day_panel hdp))
    LOOP
        UPDATE case_hearing_request
        SET is_a_panel_flag = false
        WHERE hearing_id = rec.hearing_id;

		INSERT INTO hearing_status_audit(hmcts_service_id,hearing_id,status,status_update_date_time,hearing_event,request_version,response_date_time,other_info)
		VALUES (rec.hmcts_service_code,rec.hearing_id,rec.status,rec.updated_date_time,'CR-97-panel_flag migration',rec.hearing_request_version,
				    CURRENT_TIMESTAMP,TO_JSON('{"HMAN Ticket": "HMAN-861", "CR": "CR-97-DB Migration"}'::JSONB));
		END LOOP;
END $$;
