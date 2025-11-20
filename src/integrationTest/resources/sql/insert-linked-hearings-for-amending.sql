INSERT INTO linked_group_details
(linked_group_id, request_id, request_name, request_date_time, link_type,
 reason_for_link, status, linked_comments, linked_group_latest_version, created_date_time)
VALUES
(100000, 100000, 'Group name', now(), 'ORDERED',
 'Group reason', 'ACTIVE', 'Group comments', 1, now());

INSERT INTO hearing
(hearing_id, status, linked_group_id, linked_order, is_linked_flag,
 error_code, error_description, created_date_time, updated_date_time, deployment_id,
 last_good_status)
VALUES
(2000000000, 'HEARING_REQUESTED', 100000, 1, true,
 null, null, '2025-10-01 12:00:00', '2025-10-01 12:30:00', null,
 null);

INSERT INTO hearing
(hearing_id, status, linked_group_id, linked_order, is_linked_flag,
 error_code, error_description, created_date_time, updated_date_time, deployment_id,
 last_good_status)
VALUES
(2000000001, 'HEARING_REQUESTED', 100000, 2, true,
 null, null, '2025-10-01 13:00:00', '2025-10-01 13:30:00', null,
 null);

INSERT INTO case_hearing_request
(case_hearing_id, auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type,
 number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be,
 hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path,
 hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag,
 case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, requester,
 hearing_window_start_date_range, hearing_window_end_date_range, listing_comments, created_date_time, listing_auto_change_reason_code,
 is_a_panel_flag)
VALUES
(1, false, 'Some hearing type', 5, 'Standard',
 0, false, false, '', null,
 'ABA1', '1111222233334444', now(), 'EXT/REF123', 'https://www.google.com',
 'Internal case name', 'Public case name', false, 'CMLC123', false,
 current_date, 1, 2000000000, false, '',
 null, null, 'Some listing comments', '2025-10-01 12:00:00', 'user-added-comments',
 null);

INSERT INTO hearing
(hearing_id, status, linked_group_id, linked_order, is_linked_flag,
 error_code, error_description, created_date_time, updated_date_time, deployment_id,
 last_good_status)
VALUES
(2000000002, 'HEARING_REQUESTED', null, null, true,
 null, null, '2025-10-01 14:00:00', '2025-10-01 14:30:00', null,
 null);

INSERT INTO hearing
(hearing_id, status, linked_group_id, linked_order, is_linked_flag,
 error_code, error_description, created_date_time, updated_date_time, deployment_id,
 last_good_status)
VALUES
(2000000003, 'HEARING_REQUESTED', null, null, true,
 null, null, '2025-10-01 15:00:00', '2025-10-01 15:30:00', null,
 null);

INSERT INTO case_hearing_request
(case_hearing_id, auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type,
 number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be,
 hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path,
 hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag,
 case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, requester,
 hearing_window_start_date_range, hearing_window_end_date_range, listing_comments, created_date_time, listing_auto_change_reason_code,
 is_a_panel_flag)
VALUES
(2, false, 'Some hearing type', 5, 'Standard',
 0, false, false, '', null,
 'ABA1', '1111222233334444', now(), 'EXT/REF123', 'https://www.google.com',
 'Internal case name', 'Public case name', false, 'CMLC123', false,
 current_date, 1, 2000000002, false, '',
 null, null, 'Some listing comments', '2025-10-01 13:00:00', 'user-added-comments',
 null);
