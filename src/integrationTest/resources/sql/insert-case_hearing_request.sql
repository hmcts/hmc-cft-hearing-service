DELETE FROM case_hearing_request;
DELETE FROM hearing;

INSERT INTO hearing (hearing_id, status) VALUES ('2000000000', 'RESPONDED');

insert into public.case_hearing_request(
case_hearing_id, auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_id, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, version_number, hearing_id, interpreter_booking_required_flag, is_linked_flag, listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range, request_timestamp)
VALUES (1, 't'	,'Some hearing type',	60,	'Priority type',	4,'f','f','AB123',null,'ABA1'	,1111222233334444,	'2021-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC123',	't',	'2021-10-10 00:00:00',	1,	2000000000	,'t'	,'f',	'Some listing comments',	'Some judge',	'2021-11-01 00:00:00',	'2021-11-12 00:00:00',	'2021-08-10 11:20:00');

