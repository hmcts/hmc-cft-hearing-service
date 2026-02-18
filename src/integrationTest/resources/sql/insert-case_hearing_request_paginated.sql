INSERT INTO hearing
(hearing_id, status, linked_group_id, linked_order, is_linked_flag,
 error_code, error_description, created_date_time, updated_date_time, deployment_id,
 last_good_status)
VALUES
(2000000000, 'LISTED', null, null, false,
 null, null, now(), now(), null,
 'LISTED');

INSERT INTO case_hearing_request
(case_hearing_id, auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type,
 number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be,
 hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path,
 hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag,
 case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, requester,
 hearing_window_start_date_range, hearing_window_end_date_range, listing_comments, created_date_time, listing_auto_change_reason_code,
 is_a_panel_flag)
VALUES
(1, false, 'Hearing type', 60, 'Priority type',
 0, false, false, 'Lead judge contract type', null,
 'TEST', '1234123412341238', '2026-01-01 13:00:00', 'External case ref', 'https://google.com',
 'Internal case name', 'Public case name', false, 'OWNLOC1', false,
 '2026-01-01 13:00:00', 1, 2000000000, false, 'Requester',
 '2026-01-02 00:00:00', '2026-01-03 00:00:00', 'Listing comments', '2026-01-01 13:00:00', null,
 false);
