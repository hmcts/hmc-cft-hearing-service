insert into hearing ( hearing_id, status) values ('2000000000', 'AWAITING_LISTING');

insert into case_hearing_request (
 auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_id, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
values ( 't'	,'hearingType1',	60,	'Priority type1',	4,'f','f','AB123',null,'ABA1'	,9372710950276233,	'2021-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC123',	't',	'2021-10-10 00:00:00',	1,	2000000000	,'t'	,	'Some listing comments1',	'Some judge1',	'2021-11-01 00:00:00',	'2021-11-12 00:00:00');

insert into hearing_response(hearing_id, received_date_time, listing_status, listing_case_status, response_version, request_version)
values ('2000000000', '2020-08-10 11:20:00', 'listingStatus1-1', 'caselistingStatus1-1', '2', '1');
